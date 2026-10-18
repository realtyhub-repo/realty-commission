package service.comisiones.repository;

import service.comisiones.enums.EstadoComision;

import java.math.BigDecimal;

public interface ResumenEstadoProyeccion {

    EstadoComision getEstado();

    Long getCantidad();

    BigDecimal getMonto();
}
