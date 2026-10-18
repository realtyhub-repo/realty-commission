package service.comisiones.dto.response;

import service.comisiones.enums.PerspectivaResumen;

import java.time.LocalDate;
import java.util.List;

public record ResumenComisionResponse(
        LocalDate desde,
        LocalDate hasta,
        PerspectivaResumen perspectiva,
        List<ResumenEstadoResponse> porEstado
) {
}
