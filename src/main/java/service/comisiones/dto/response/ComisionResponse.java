package service.comisiones.dto.response;

import service.comisiones.enums.EstadoComision;
import service.comisiones.enums.TipoContrato;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ComisionResponse(
        UUID id,
        UUID contratoId,
        UUID leadId,
        UUID propiedadId,
        UUID clienteId,
        UUID agenteId,
        String nombreAgente,
        UUID oficinaId,
        String nombreOficina,
        TipoContrato tipoContrato,
        BigDecimal precioBase,
        BigDecimal montoTotal,
        EstadoComision estado,
        LocalDateTime firmadoEn,
        LocalDateTime aprobadaEn,
        LocalDateTime pagadaEn,
        String referenciaPago,
        LocalDateTime createdAt,
        List<RepartoResponse> reparto
) {
}
