package service.comisiones.entity;

import jakarta.persistence.*;
import lombok.*;
import service.comisiones.enums.EstadoComision;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comision_historial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComisionHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "comision_id", nullable = false, updatable = false)
    private UUID comisionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", updatable = false, length = 20)
    private EstadoComision estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, updatable = false, length = 20)
    private EstadoComision estadoNuevo;

    @Column(name = "usuario_id", updatable = false)
    private UUID usuarioId;

    @Column(length = 500, updatable = false)
    private String motivo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
