package service.comisiones.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.comisiones.dto.event.ContractCancelledEvent;
import service.comisiones.dto.event.ContractSignedEvent;
import service.comisiones.dto.response.AgentePerfilInternalResponse;
import service.comisiones.dto.response.OficinaInternalResponse;
import service.comisiones.entity.AcuerdoComercial;
import service.comisiones.entity.Comision;
import service.comisiones.entity.ComisionReparto;
import service.comisiones.entity.ReglaComision;
import service.comisiones.enums.BeneficiarioTipo;
import service.comisiones.enums.EstadoComision;
import service.comisiones.enums.TipoOficina;
import service.comisiones.exceptions.ComisionNoEncontradaException;
import service.comisiones.exceptions.NoReintentableException;
import service.comisiones.exceptions.OficinaNoEncontradaException;
import service.comisiones.exceptions.SinAcuerdoVigenteException;
import service.comisiones.repository.AcuerdoComercialRepository;
import service.comisiones.repository.ComisionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComisionRegistroService {

    private static final String NOMBRE_AGENTE_POR_DEFECTO = "Agente";

    private final ComisionRepository comisionRepository;
    private final AcuerdoComercialRepository acuerdoRepository;
    private final UsuarioClientService usuarioClient;
    private final ComisionCalculador calculador;
    private final ComisionTransiciones transiciones;

    @Transactional
    public void registrarDesdeContrato(ContractSignedEvent evento) {
        if (comisionRepository.existsByContratoId(evento.contratoId())) {
            log.info("La comisión del contrato {} ya existe, se ignora el evento repetido", evento.contratoId());
            return;
        }

        validarEvento(evento);

        LocalDate fechaFirma = evento.firmadoEn().toLocalDate();

        AcuerdoComercial acuerdo = acuerdoRepository.buscarVigente(fechaFirma)
                .orElseThrow(() -> new SinAcuerdoVigenteException(
                        "No hay acuerdo comercial vigente para el " + fechaFirma));

        ReglaComision regla = acuerdo.getReglas().stream()
                .filter(r -> r.getTipoContrato() == evento.tipo())
                .findFirst()
                .orElseThrow(() -> new SinAcuerdoVigenteException(
                        "El acuerdo '" + acuerdo.getNombre() + "' no define regla para " + evento.tipo()));

        AgentePerfilInternalResponse agente = usuarioClient.buscarAgentePorUsuario(evento.agenteId());

        if (agente.oficinaId() == null) {
            throw new OficinaNoEncontradaException("El agente " + evento.agenteId() + " no tiene oficina asignada");
        }

        OficinaInternalResponse oficina = usuarioClient.buscarOficina(agente.oficinaId());
        OficinaInternalResponse central = resolverCentral(oficina);

        ComisionCalculador.Resultado resultado = calculador.calcular(regla, evento.precioAcordado());

        Comision comision = Comision.builder()
                .contratoId(evento.contratoId())
                .leadId(evento.leadId())
                .propiedadId(evento.propiedadId())
                .clienteId(evento.clienteId())
                .agenteId(evento.agenteId())
                .oficinaId(oficina.id())
                .centralId(central.id())
                .gerenteOficinaId(oficina.gerenteId())
                .acuerdoId(acuerdo.getId())
                .reglaId(regla.getId())
                .tipoContrato(evento.tipo())
                .baseCalculo(regla.getBaseCalculo())
                .valorRegla(regla.getValor())
                .precioBase(evento.precioAcordado())
                .montoTotal(resultado.montoTotal())
                .estado(EstadoComision.PENDIENTE)
                .firmadoEn(evento.firmadoEn())
                .build();

        String nombreAgente = agente.nombreUsuario() != null ? agente.nombreUsuario() : NOMBRE_AGENTE_POR_DEFECTO;

        comision.agregarReparto(linea(BeneficiarioTipo.AGENTE, evento.agenteId(), nombreAgente,
                regla.getPorcentajeAgente(), resultado.montos().get(BeneficiarioTipo.AGENTE)));
        comision.agregarReparto(linea(BeneficiarioTipo.OFICINA, oficina.id(), oficina.nombre(),
                regla.getPorcentajeOficina(), resultado.montos().get(BeneficiarioTipo.OFICINA)));
        comision.agregarReparto(linea(BeneficiarioTipo.CENTRAL, central.id(), central.nombre(),
                regla.getPorcentajeCentral(), resultado.montos().get(BeneficiarioTipo.CENTRAL)));

        comisionRepository.save(comision);
        transiciones.registrarCreacion(comision);

        log.info("Comisión {} creada para el contrato {} por {}", comision.getId(), evento.contratoId(),
                comision.getMontoTotal());
    }

    @Transactional
    public void anularPorContrato(ContractCancelledEvent evento) {
        Comision comision = comisionRepository.findByContratoId(evento.contratoId())
                .orElseThrow(() -> new ComisionNoEncontradaException(
                        "No existe comisión para el contrato " + evento.contratoId()));

        EstadoComision destino = switch (comision.getEstado()) {
            case PENDIENTE, APROBADA -> EstadoComision.ANULADA;
            case PAGADA -> EstadoComision.REVERTIDA;
            case ANULADA, REVERTIDA -> null;
        };

        if (destino == null) {
            log.info("La comisión {} ya estaba {}, se ignora el evento repetido",
                    comision.getId(), comision.getEstado());
            return;
        }

        transiciones.aplicar(comision, destino, null, "Contrato cancelado");
    }

    private void validarEvento(ContractSignedEvent evento) {
        if (evento.tipo() == null
                || evento.firmadoEn() == null
                || evento.agenteId() == null
                || evento.precioAcordado() == null
                || evento.precioAcordado().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NoReintentableException("El evento contract.signed del contrato "
                    + evento.contratoId() + " está incompleto o tiene un precio inválido");
        }
    }

    private OficinaInternalResponse resolverCentral(OficinaInternalResponse oficina) {
        if (oficina.tipo() == TipoOficina.CENTRAL) {
            return oficina;
        }
        if (oficina.oficinaCentralId() == null) {
            throw new OficinaNoEncontradaException(
                    "La sucursal " + oficina.id() + " no tiene oficina central asociada");
        }
        return usuarioClient.buscarOficina(oficina.oficinaCentralId());
    }

    private ComisionReparto linea(BeneficiarioTipo tipo, UUID beneficiarioId, String nombre,
                                  BigDecimal porcentaje, BigDecimal monto) {
        return ComisionReparto.builder()
                .beneficiarioTipo(tipo)
                .beneficiarioId(beneficiarioId)
                .beneficiarioNombre(nombre)
                .porcentaje(porcentaje)
                .monto(monto)
                .build();
    }
}
