package service.comisiones.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AcuerdoResponse(
        UUID id,
        String nombre,
        LocalDate vigenteDesde,
        LocalDate vigenteHasta,
        UUID creadoPor,
        LocalDateTime createdAt,
        List<ReglaResponse> reglas
) {
}
