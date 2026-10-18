package service.comisiones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PagarComisionRequest(

        @NotBlank
        @Size(max = 120)
        String referenciaPago
) {
}
