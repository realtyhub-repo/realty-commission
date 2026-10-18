package service.comisiones.dto.response;

import java.util.UUID;

public record AgentePerfilInternalResponse(
        UUID id,
        UUID usuarioId,
        String nombreUsuario,
        String urlFoto,
        UUID oficinaId,
        String nombreOficina,
        String zonaEspecialidad
) {
}
