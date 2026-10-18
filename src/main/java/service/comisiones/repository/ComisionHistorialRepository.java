package service.comisiones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.comisiones.entity.ComisionHistorial;

import java.util.List;
import java.util.UUID;

public interface ComisionHistorialRepository extends JpaRepository<ComisionHistorial, UUID> {

    List<ComisionHistorial> findByComisionIdOrderByCreatedAtAsc(UUID comisionId);
}
