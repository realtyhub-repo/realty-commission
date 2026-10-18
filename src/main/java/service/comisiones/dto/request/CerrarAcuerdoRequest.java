package service.comisiones.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CerrarAcuerdoRequest(

        @NotNull
        LocalDate vigenteHasta
) {
}
