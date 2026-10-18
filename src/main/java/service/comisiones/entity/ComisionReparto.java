package service.comisiones.entity;

import jakarta.persistence.*;
import lombok.*;
import service.comisiones.enums.BeneficiarioTipo;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "comision_reparto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComisionReparto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comision_id", nullable = false, updatable = false)
    private Comision comision;

    @Enumerated(EnumType.STRING)
    @Column(name = "beneficiario_tipo", nullable = false, updatable = false, length = 10)
    private BeneficiarioTipo beneficiarioTipo;

    @Column(name = "beneficiario_id", nullable = false, updatable = false)
    private UUID beneficiarioId;

    @Column(name = "beneficiario_nombre", nullable = false, updatable = false, length = 200)
    private String beneficiarioNombre;

    @Column(nullable = false, updatable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(nullable = false, updatable = false, precision = 15, scale = 2)
    private BigDecimal monto;
}
