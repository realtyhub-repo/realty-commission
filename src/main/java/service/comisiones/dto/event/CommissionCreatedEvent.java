package service.comisiones.dto.event;

import service.comisiones.enums.BeneficiarioTipo;
import service.comisiones.enums.TipoContrato;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CommissionCreatedEvent(
        UUID comisionId,
        UUID contratoId,
        UUID propiedadId,
        UUID agenteId,
        UUID oficinaId,
        UUID centralId,
        UUID gerenteOficinaId,
        TipoContrato tipoContrato,
        BigDecimal montoTotal,
        List<Reparto> reparto,
        LocalDateTime creadaEn
) {

    public record Reparto(BeneficiarioTipo tipo, UUID beneficiarioId, BigDecimal monto) {
    }
}
