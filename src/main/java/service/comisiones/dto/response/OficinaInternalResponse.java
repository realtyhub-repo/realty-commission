package service.comisiones.dto.response;

import service.comisiones.enums.TipoOficina;

import java.util.UUID;

public record OficinaInternalResponse(
        UUID id,
        String nombre,
        String region,
        Double latitud,
        Double longitud,
        TipoOficina tipo,
        UUID oficinaCentralId,
        UUID gerenteId
) {
}
