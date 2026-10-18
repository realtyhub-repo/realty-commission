package service.comisiones.dto.response;

import service.comisiones.enums.EstadoComision;

import java.math.BigDecimal;

public record ResumenEstadoResponse(
        EstadoComision estado,
        long cantidad,
        BigDecimal monto
) {
}
