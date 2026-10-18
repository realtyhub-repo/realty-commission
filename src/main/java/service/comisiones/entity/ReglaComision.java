package service.comisiones.entity;

import jakarta.persistence.*;
import lombok.*;
import service.comisiones.enums.BaseCalculo;
import service.comisiones.enums.TipoContrato;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "regla_comision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglaComision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acuerdo_id", nullable = false, updatable = false)
    private AcuerdoComercial acuerdo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato", nullable = false, updatable = false, length = 20)
    private TipoContrato tipoContrato;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_calculo", nullable = false, updatable = false, length = 20)
    private BaseCalculo baseCalculo;

    @Column(nullable = false, updatable = false, precision = 12, scale = 4)
    private BigDecimal valor;

    @Column(name = "porcentaje_agente", nullable = false, updatable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeAgente;

    @Column(name = "porcentaje_oficina", nullable = false, updatable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeOficina;

    @Column(name = "porcentaje_central", nullable = false, updatable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeCentral;
}
