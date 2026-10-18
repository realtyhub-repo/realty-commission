package service.comisiones.service;

import org.springframework.stereotype.Component;
import service.comisiones.entity.ReglaComision;
import service.comisiones.enums.BeneficiarioTipo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ComisionCalculador {

    private static final List<BeneficiarioTipo> ORDEN = List.of(
            BeneficiarioTipo.AGENTE, BeneficiarioTipo.OFICINA, BeneficiarioTipo.CENTRAL);

    public record Resultado(BigDecimal montoTotal, Map<BeneficiarioTipo, BigDecimal> montos) {
    }

    public Resultado calcular(ReglaComision regla, BigDecimal precio) {
        BigDecimal total = calcularTotal(regla, precio);
        return new Resultado(total, repartir(total, regla));
    }

    private BigDecimal calcularTotal(ReglaComision regla, BigDecimal precio) {
        BigDecimal bruto = switch (regla.getBaseCalculo()) {
            case PORCENTAJE_PRECIO -> precio.multiply(regla.getValor()).movePointLeft(2);
            case MESES_CANON -> precio.multiply(regla.getValor());
        };
        return bruto.setScale(0, RoundingMode.HALF_UP);
    }

    private Map<BeneficiarioTipo, BigDecimal> repartir(BigDecimal total, ReglaComision regla) {
        Map<BeneficiarioTipo, BigDecimal> porcentajes = new EnumMap<>(BeneficiarioTipo.class);
        porcentajes.put(BeneficiarioTipo.AGENTE, regla.getPorcentajeAgente());
        porcentajes.put(BeneficiarioTipo.OFICINA, regla.getPorcentajeOficina());
        porcentajes.put(BeneficiarioTipo.CENTRAL, regla.getPorcentajeCentral());

        Map<BeneficiarioTipo, BigDecimal> montos = new EnumMap<>(BeneficiarioTipo.class);
        Map<BeneficiarioTipo, BigDecimal> fracciones = new EnumMap<>(BeneficiarioTipo.class);
        BigDecimal asignado = BigDecimal.ZERO;

        for (BeneficiarioTipo tipo : ORDEN) {
            BigDecimal exacto = total.multiply(porcentajes.get(tipo)).movePointLeft(2);
            BigDecimal base = exacto.setScale(0, RoundingMode.DOWN);
            montos.put(tipo, base);
            fracciones.put(tipo, exacto.subtract(base));
            asignado = asignado.add(base);
        }

        int sobrante = total.subtract(asignado).intValueExact();

        List<BeneficiarioTipo> porFraccion = ORDEN.stream()
                .sorted(Comparator.comparing((BeneficiarioTipo tipo) -> fracciones.get(tipo)).reversed())
                .toList();

        for (int i = 0; i < sobrante; i++) {
            montos.merge(porFraccion.get(i), BigDecimal.ONE, BigDecimal::add);
        }

        return montos;
    }
}
