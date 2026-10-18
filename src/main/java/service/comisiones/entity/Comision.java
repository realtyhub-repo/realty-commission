package service.comisiones.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import service.comisiones.enums.BaseCalculo;
import service.comisiones.enums.EstadoComision;
import service.comisiones.enums.TipoContrato;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "comision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "contrato_id", nullable = false, updatable = false)
    private UUID contratoId;

    @Column(name = "lead_id", nullable = false, updatable = false)
    private UUID leadId;

    @Column(name = "propiedad_id", nullable = false, updatable = false)
    private UUID propiedadId;

    @Column(name = "cliente_id", nullable = false, updatable = false)
    private UUID clienteId;

    @Column(name = "agente_id", nullable = false, updatable = false)
    private UUID agenteId;

    @Column(name = "oficina_id", nullable = false, updatable = false)
    private UUID oficinaId;

    @Column(name = "central_id", nullable = false, updatable = false)
    private UUID centralId;

    @Column(name = "gerente_oficina_id", updatable = false)
    private UUID gerenteOficinaId;

    @Column(name = "acuerdo_id", nullable = false, updatable = false)
    private UUID acuerdoId;

    @Column(name = "regla_id", nullable = false, updatable = false)
    private UUID reglaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato", nullable = false, updatable = false, length = 20)
    private TipoContrato tipoContrato;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_calculo", nullable = false, updatable = false, length = 20)
    private BaseCalculo baseCalculo;

    @Column(name = "valor_regla", nullable = false, updatable = false, precision = 12, scale = 4)
    private BigDecimal valorRegla;

    @Column(name = "precio_base", nullable = false, updatable = false, precision = 15, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "monto_total", nullable = false, updatable = false, precision = 15, scale = 2)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoComision estado;

    @Column(name = "firmado_en", nullable = false, updatable = false)
    private LocalDateTime firmadoEn;

    @Column(name = "aprobada_por")
    private UUID aprobadaPor;

    @Column(name = "aprobada_en")
    private LocalDateTime aprobadaEn;

    @Column(name = "pagada_por")
    private UUID pagadaPor;

    @Column(name = "pagada_en")
    private LocalDateTime pagadaEn;

    @Column(name = "referencia_pago", length = 120)
    private String referenciaPago;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "comision", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<ComisionReparto> reparto = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void agregarReparto(ComisionReparto linea) {
        linea.setComision(this);
        this.reparto.add(linea);
    }
}
