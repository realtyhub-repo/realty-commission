package service.comisiones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import service.comisiones.entity.OutboxEvento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxEventoRepository extends JpaRepository<OutboxEvento, UUID> {

    @Query(value = """
            SELECT * FROM outbox_evento
            WHERE publicado_en IS NULL
            ORDER BY created_at
            LIMIT :lote
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvento> reclamarPendientes(@Param("lote") int lote);

    @Modifying
    @Query("delete from OutboxEvento o where o.publicadoEn is not null and o.publicadoEn < :limite")
    int eliminarPublicadosAnteriores(@Param("limite") LocalDateTime limite);
}
