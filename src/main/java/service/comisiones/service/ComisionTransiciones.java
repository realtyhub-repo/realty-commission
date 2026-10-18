package service.comisiones.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import service.comisiones.config.RabbitConfig;
import service.comisiones.dto.event.CommissionCreatedEvent;
import service.comisiones.dto.event.CommissionStatusEvent;
import service.comisiones.entity.Comision;
import service.comisiones.entity.ComisionHistorial;
import service.comisiones.enums.EstadoComision;
import service.comisiones.exceptions.TransicionInvalidaException;
import service.comisiones.repository.ComisionHistorialRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ComisionTransiciones {

    private final ComisionHistorialRepository historialRepository;
    private final OutboxService outboxService;

    public void registrarCreacion(Comision comision) {
        historialRepository.save(ComisionHistorial.builder()
                .comisionId(comision.getId())
                .estadoAnterior(null)
                .estadoNuevo(EstadoComision.PENDIENTE)
                .usuarioId(null)
                .motivo("Contrato firmado")
                .build());

        List<CommissionCreatedEvent.Reparto> reparto = comision.getReparto().stream()
                .map(linea -> new CommissionCreatedEvent.Reparto(
                        linea.getBeneficiarioTipo(), linea.getBeneficiarioId(), linea.getMonto()))
                .toList();

        outboxService.registrar(RabbitConfig.RK_COMMISSION_CREATED, new CommissionCreatedEvent(
                comision.getId(),
                comision.getContratoId(),
                comision.getPropiedadId(),
                comision.getAgenteId(),
                comision.getOficinaId(),
                comision.getCentralId(),
                comision.getTipoContrato(),
                comision.getMontoTotal(),
                reparto,
                LocalDateTime.now()));
    }

    public void aplicar(Comision comision, EstadoComision destino, UUID usuarioId, String motivo) {
        EstadoComision origen = comision.getEstado();

        if (!origen.puedePasarA(destino)) {
            throw new TransicionInvalidaException(
                    "La comisión no puede pasar de " + origen + " a " + destino);
        }

        comision.setEstado(destino);

        historialRepository.save(ComisionHistorial.builder()
                .comisionId(comision.getId())
                .estadoAnterior(origen)
                .estadoNuevo(destino)
                .usuarioId(usuarioId)
                .motivo(motivo)
                .build());

        outboxService.registrar(routingKey(destino), new CommissionStatusEvent(
                comision.getId(),
                comision.getContratoId(),
                comision.getAgenteId(),
                destino,
                comision.getMontoTotal(),
                LocalDateTime.now()));
    }

    private String routingKey(EstadoComision destino) {
        return switch (destino) {
            case APROBADA -> RabbitConfig.RK_COMMISSION_APPROVED;
            case PAGADA -> RabbitConfig.RK_COMMISSION_PAID;
            case ANULADA -> RabbitConfig.RK_COMMISSION_ANNULLED;
            case REVERTIDA -> RabbitConfig.RK_COMMISSION_REVERTED;
            case PENDIENTE -> throw new IllegalStateException("PENDIENTE solo se asigna al crear la comisión");
        };
    }
}
