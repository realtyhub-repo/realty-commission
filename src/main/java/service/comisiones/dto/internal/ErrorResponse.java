package service.comisiones.dto.internal;

import java.time.LocalDateTime;

public record ErrorResponse(
        String codigo,
        String mensaje,
        int status,
        LocalDateTime timestamp
) {
}
