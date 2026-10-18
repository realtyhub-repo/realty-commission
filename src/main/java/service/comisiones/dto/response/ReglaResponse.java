package service.comisiones.dto.response;

import service.comisiones.enums.BaseCalculo;
import service.comisiones.enums.TipoContrato;

import java.math.BigDecimal;
import java.util.UUID;

public record ReglaResponse(
        UUID id,
        TipoContrato tipoContrato,
        BaseCalculo baseCalculo,
        BigDecimal valor,
        BigDecimal porcentajeAgente,
        BigDecimal porcentajeOficina,
        BigDecimal porcentajeCentral
) {
}
