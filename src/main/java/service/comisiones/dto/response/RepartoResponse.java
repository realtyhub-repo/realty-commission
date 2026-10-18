package service.comisiones.dto.response;

import service.comisiones.enums.BeneficiarioTipo;

import java.math.BigDecimal;
import java.util.UUID;

public record RepartoResponse(
        BeneficiarioTipo beneficiarioTipo,
        UUID beneficiarioId,
        String beneficiarioNombre,
        BigDecimal porcentaje,
        BigDecimal monto
) {
}
