package service.comisiones.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import service.comisiones.entity.AcuerdoComercial;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcuerdoComercialRepository extends JpaRepository<AcuerdoComercial, UUID> {

    @EntityGraph(attributePaths = "reglas")
    List<AcuerdoComercial> findAllByOrderByVigenteDesdeDesc();

    @Override
    @EntityGraph(attributePaths = "reglas")
    Optional<AcuerdoComercial> findById(UUID id);

    @EntityGraph(attributePaths = "reglas")
    @Query("""
            select a from AcuerdoComercial a
            where a.vigenteDesde <= :fecha
              and (a.vigenteHasta is null or a.vigenteHasta >= :fecha)
            """)
    Optional<AcuerdoComercial> buscarVigente(@Param("fecha") LocalDate fecha);

    @Query("""
            select count(a) from AcuerdoComercial a
            where a.vigenteDesde <= :hasta
              and (a.vigenteHasta is null or a.vigenteHasta >= :desde)
            """)
    long contarSolapadosConRango(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("""
            select count(a) from AcuerdoComercial a
            where a.vigenteHasta is null or a.vigenteHasta >= :desde
            """)
    long contarSolapadosDesde(@Param("desde") LocalDate desde);
}
