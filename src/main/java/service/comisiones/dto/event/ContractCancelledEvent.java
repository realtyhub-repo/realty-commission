package service.comisiones.dto.event;

import java.util.UUID;

public record ContractCancelledEvent(
        UUID contratoId,
        UUID leadId,
        UUID propiedadId,
        UUID clienteId,
        UUID agenteId
) {
}
