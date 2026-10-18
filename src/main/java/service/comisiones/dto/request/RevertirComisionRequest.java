package service.comisiones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RevertirComisionRequest(

        @NotBlank
        @Size(max = 500)
        String motivo
) {
}
