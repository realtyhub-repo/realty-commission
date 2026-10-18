package service.comisiones.service;

import org.springframework.stereotype.Component;
import service.comisiones.dto.response.ComisionDetalleResponse;
import service.comisiones.dto.response.ComisionResponse;
import service.comisiones.dto.response.HistorialResponse;
import service.comisiones.dto.response.RepartoResponse;
import service.comisiones.entity.Comision;
import service.comisiones.entity.ComisionHistorial;
import service.comisiones.entity.ComisionReparto;
import service.comisiones.enums.BeneficiarioTipo;
import service.comisiones.enums.RolUsuario;

import java.util.Comparator;
import java.util.List;

@Component
public class ComisionMapper {

    public ComisionResponse aResumen(Comision c, RolUsuario rol) {
        return new ComisionResponse(
                c.getId(),
                c.getContratoId(),
                c.getLeadId(),
                c.getPropiedadId(),
                c.getClienteId(),
                c.getAgenteId(),
                nombreDe(c, BeneficiarioTipo.AGENTE),
                c.getOficinaId(),
                nombreDe(c, BeneficiarioTipo.OFICINA),
                c.getTipoContrato(),
                c.getPrecioBase(),
                c.getMontoTotal(),
                c.getEstado(),
                c.getFirmadoEn(),
                c.getAprobadaEn(),
                c.getPagadaEn(),
                c.getReferenciaPago(),
                c.getCreatedAt(),
                reparto(c, rol));
    }

    public ComisionDetalleResponse aDetalle(Comision c, List<ComisionHistorial> historial, RolUsuario rol) {
        return new ComisionDetalleResponse(
                c.getId(),
                c.getContratoId(),
                c.getLeadId(),
                c.getPropiedadId(),
                c.getClienteId(),
                c.getAgenteId(),
                nombreDe(c, BeneficiarioTipo.AGENTE),
                c.getOficinaId(),
                nombreDe(c, BeneficiarioTipo.OFICINA),
                c.getCentralId(),
                c.getTipoContrato(),
                c.getBaseCalculo(),
                c.getValorRegla(),
                c.getAcuerdoId(),
                c.getPrecioBase(),
                c.getMontoTotal(),
                c.getEstado(),
                c.getFirmadoEn(),
                c.getAprobadaPor(),
                c.getAprobadaEn(),
                c.getPagadaPor(),
                c.getPagadaEn(),
                c.getReferenciaPago(),
                c.getCreatedAt(),
                reparto(c, rol),
                historial.stream()
                        .map(h -> new HistorialResponse(
                                h.getEstadoAnterior(),
                                h.getEstadoNuevo(),
                                h.getUsuarioId(),
                                h.getMotivo(),
                                h.getCreatedAt()))
                        .toList());
    }

    private List<RepartoResponse> reparto(Comision c, RolUsuario rol) {
        return c.getReparto().stream()
                .filter(linea -> rol != RolUsuario.AGENTE || linea.getBeneficiarioTipo() == BeneficiarioTipo.AGENTE)
                .sorted(Comparator.comparing(ComisionReparto::getBeneficiarioTipo))
                .map(linea -> new RepartoResponse(
                        linea.getBeneficiarioTipo(),
                        linea.getBeneficiarioId(),
                        linea.getBeneficiarioNombre(),
                        linea.getPorcentaje(),
                        linea.getMonto()))
                .toList();
    }

    private String nombreDe(Comision c, BeneficiarioTipo tipo) {
        return c.getReparto().stream()
                .filter(linea -> linea.getBeneficiarioTipo() == tipo)
                .map(ComisionReparto::getBeneficiarioNombre)
                .findFirst()
                .orElse(null);
    }
}
