package service.comisiones.dto.event;

import service.comisiones.enums.EstadoComision;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommissionStatusEvent(
        UUID comisionId,
        UUID contratoId,
        UUID agenteId,
        EstadoComision estado,
        BigDecimal montoTotal,
        LocalDateTime ocurridoEn
) {
}
