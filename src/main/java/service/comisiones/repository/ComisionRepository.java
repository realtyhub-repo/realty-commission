package service.comisiones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import service.comisiones.entity.Comision;
import service.comisiones.enums.BeneficiarioTipo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComisionRepository extends JpaRepository<Comision, UUID>, JpaSpecificationExecutor<Comision> {

    boolean existsByContratoId(UUID contratoId);

    Optional<Comision> findByContratoId(UUID contratoId);

    @Query("""
            select c.estado as estado, count(c) as cantidad, sum(c.montoTotal) as monto
            from Comision c
            where c.firmadoEn >= :desde and c.firmadoEn < :hasta
            group by c.estado
            """)
    List<ResumenEstadoProyeccion> resumenTotal(@Param("desde") LocalDateTime desde,
                                               @Param("hasta") LocalDateTime hasta);

    @Query("""
            select c.estado as estado, count(c) as cantidad, sum(r.monto) as monto
            from Comision c join c.reparto r
            where c.agenteId = :agenteId
              and r.beneficiarioTipo = :tipo
              and c.firmadoEn >= :desde and c.firmadoEn < :hasta
            group by c.estado
            """)
    List<ResumenEstadoProyeccion> resumenPorAgente(@Param("agenteId") UUID agenteId,
                                                   @Param("tipo") BeneficiarioTipo tipo,
                                                   @Param("desde") LocalDateTime desde,
                                                   @Param("hasta") LocalDateTime hasta);

    @Query("""
            select c.estado as estado, count(c) as cantidad, sum(r.monto) as monto
            from Comision c join c.reparto r
            where c.gerenteOficinaId = :gerenteId
              and r.beneficiarioTipo = :tipo
              and c.firmadoEn >= :desde and c.firmadoEn < :hasta
            group by c.estado
            """)
    List<ResumenEstadoProyeccion> resumenPorGerente(@Param("gerenteId") UUID gerenteId,
                                                    @Param("tipo") BeneficiarioTipo tipo,
                                                    @Param("desde") LocalDateTime desde,
                                                    @Param("hasta") LocalDateTime hasta);
}
