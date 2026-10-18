package service.comisiones.repository;

import org.springframework.data.jpa.domain.Specification;
import service.comisiones.entity.Comision;
import service.comisiones.enums.EstadoComision;

import java.time.LocalDateTime;
import java.util.UUID;

public final class ComisionSpecifications {

    private ComisionSpecifications() {
    }

    public static Specification<Comision> sinRestriccion() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Comision> porAgente(UUID agenteId) {
        return (root, query, cb) -> cb.equal(root.get("agenteId"), agenteId);
    }

    public static Specification<Comision> porGerente(UUID gerenteId) {
        return (root, query, cb) -> cb.equal(root.get("gerenteOficinaId"), gerenteId);
    }

    public static Specification<Comision> conEstado(EstadoComision estado) {
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    public static Specification<Comision> firmadaDesde(LocalDateTime desde) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("firmadoEn"), desde);
    }

    public static Specification<Comision> firmadaAntesDe(LocalDateTime hasta) {
        return (root, query, cb) -> cb.lessThan(root.get("firmadoEn"), hasta);
    }
}
