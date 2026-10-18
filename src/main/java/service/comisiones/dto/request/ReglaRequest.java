package service.comisiones.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import service.comisiones.enums.BaseCalculo;
import service.comisiones.enums.TipoContrato;

import java.math.BigDecimal;

public record ReglaRequest(

        @NotNull
        TipoContrato tipoContrato,

        @NotNull
        BaseCalculo baseCalculo,

        @NotNull
        @DecimalMin("0.0001")
        @Digits(integer = 8, fraction = 4)
        BigDecimal valor,

        @NotNull
        @DecimalMin("0")
        @DecimalMax("100")
        @Digits(integer = 3, fraction = 2)
        BigDecimal porcentajeAgente,

        @NotNull
        @DecimalMin("0")
        @DecimalMax("100")
        @Digits(integer = 3, fraction = 2)
        BigDecimal porcentajeOficina,

        @NotNull
        @DecimalMin("0")
        @DecimalMax("100")
        @Digits(integer = 3, fraction = 2)
        BigDecimal porcentajeCentral
) {
}
