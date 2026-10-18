package service.comisiones.dto.response;

import service.comisiones.enums.BaseCalculo;
import service.comisiones.enums.EstadoComision;
import service.comisiones.enums.TipoContrato;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ComisionDetalleResponse(
        UUID id,
        UUID contratoId,
        UUID leadId,
        UUID propiedadId,
        UUID clienteId,
        UUID agenteId,
        String nombreAgente,
        UUID oficinaId,
        String nombreOficina,
        UUID centralId,
        TipoContrato tipoContrato,
        BaseCalculo baseCalculo,
        BigDecimal valorRegla,
        UUID acuerdoId,
        BigDecimal precioBase,
        BigDecimal montoTotal,
        EstadoComision estado,
        LocalDateTime firmadoEn,
        UUID aprobadaPor,
        LocalDateTime aprobadaEn,
        UUID pagadaPor,
        LocalDateTime pagadaEn,
        String referenciaPago,
        LocalDateTime createdAt,
        List<RepartoResponse> reparto,
        List<HistorialResponse> historial
) {
}
