package service.comisiones.dto.response;

import service.comisiones.enums.EstadoComision;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistorialResponse(
        EstadoComision estadoAnterior,
        EstadoComision estadoNuevo,
        UUID usuarioId,
        String motivo,
        LocalDateTime createdAt
) {
}
