package service.comisiones.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.comisiones.dto.response.ComisionDetalleResponse;
import service.comisiones.dto.response.ComisionResponse;
import service.comisiones.dto.response.PaginaResponse;
import service.comisiones.dto.response.ResumenComisionResponse;
import service.comisiones.dto.response.ResumenEstadoResponse;
import service.comisiones.entity.Comision;
import service.comisiones.enums.BeneficiarioTipo;
import service.comisiones.enums.EstadoComision;
import service.comisiones.enums.PerspectivaResumen;
import service.comisiones.enums.RolUsuario;
import service.comisiones.exceptions.AccesoNoAutorizadoException;
import service.comisiones.exceptions.ComisionNoEncontradaException;
import service.comisiones.exceptions.RangoFechasInvalidoException;
import service.comisiones.repository.ComisionHistorialRepository;
import service.comisiones.repository.ComisionRepository;
import service.comisiones.repository.ComisionSpecifications;
import service.comisiones.repository.ResumenEstadoProyeccion;
import service.comisiones.security.ContextoUsuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComisionService {

    private static final int TAMANIO_MAXIMO = 100;
    private static final int TAMANIO_POR_DEFECTO = 20;

    private final ComisionRepository comisionRepository;
    private final ComisionHistorialRepository historialRepository;
    private final ComisionTransiciones transiciones;
    private final ComisionMapper mapper;

    @Transactional(readOnly = true)
    public PaginaResponse<ComisionResponse> listar(ContextoUsuario contexto, EstadoComision estado,
                                                   LocalDate desde, LocalDate hasta, int pagina, int tamanio) {
        validarRango(desde, hasta);

        Specification<Comision> filtro = alcance(contexto);

        if (estado != null) {
            filtro = filtro.and(ComisionSpecifications.conEstado(estado));
        }
        if (desde != null) {
            filtro = filtro.and(ComisionSpecifications.firmadaDesde(desde.atStartOfDay()));
        }
        if (hasta != null) {
            filtro = filtro.and(ComisionSpecifications.firmadaAntesDe(hasta.plusDays(1).atStartOfDay()));
        }

        int paginaSegura = Math.max(pagina, 0);
        int tamanioSeguro = tamanio < 1 ? TAMANIO_POR_DEFECTO : Math.min(tamanio, TAMANIO_MAXIMO);

        Pageable pageable = PageRequest.of(paginaSegura, tamanioSeguro,
                Sort.by(Sort.Direction.DESC, "firmadoEn").and(Sort.by("id")));

        Page<Comision> resultado = comisionRepository.findAll(filtro, pageable);

        return PaginaResponse.de(resultado.map(c -> mapper.aResumen(c, contexto.rol())));
    }

    @Transactional(readOnly = true)
    public ComisionDetalleResponse obtener(UUID id, ContextoUsuario contexto) {
        Comision comision = buscar(id);
        verificarAcceso(comision, contexto);

        return mapper.aDetalle(comision,
                historialRepository.findByComisionIdOrderByCreatedAtAsc(id), contexto.rol());
    }

    @Transactional
    public ComisionResponse aprobar(UUID id, ContextoUsuario contexto) {
        Comision comision = buscar(id);

        boolean esAdmin = contexto.rol() == RolUsuario.ADMINISTRADOR_CENTRAL;
        boolean esGerenteDeLaOficina = contexto.rol() == RolUsuario.GERENTE_OFICINA
                && contexto.userId().equals(comision.getGerenteOficinaId());

        if (!esAdmin && !esGerenteDeLaOficina) {
            throw new AccesoNoAutorizadoException("No tienes permiso para aprobar esta comisión");
        }
        if (comision.getAgenteId().equals(contexto.userId())) {
            throw new AccesoNoAutorizadoException("No puedes aprobar tu propia comisión");
        }

        transiciones.aplicar(comision, EstadoComision.APROBADA, contexto.userId(), null);
        comision.setAprobadaPor(contexto.userId());
        comision.setAprobadaEn(LocalDateTime.now());

        return mapper.aResumen(comision, contexto.rol());
    }

    @Transactional
    public ComisionResponse pagar(UUID id, String referenciaPago, ContextoUsuario contexto) {
        exigirAdministrador(contexto, "Solo el administrador central puede registrar pagos");

        Comision comision = buscar(id);

        transiciones.aplicar(comision, EstadoComision.PAGADA, contexto.userId(),
                "Referencia de pago: " + referenciaPago);
        comision.setPagadaPor(contexto.userId());
        comision.setPagadaEn(LocalDateTime.now());
        comision.setReferenciaPago(referenciaPago);

        return mapper.aResumen(comision, contexto.rol());
    }

    @Transactional
    public ComisionResponse revertir(UUID id, String motivo, ContextoUsuario contexto) {
        exigirAdministrador(contexto, "Solo el administrador central puede revertir pagos");

        Comision comision = buscar(id);

        transiciones.aplicar(comision, EstadoComision.REVERTIDA, contexto.userId(), motivo);

        return mapper.aResumen(comision, contexto.rol());
    }

    @Transactional(readOnly = true)
    public ResumenComisionResponse resumen(ContextoUsuario contexto, LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfYear(1);
        validarRango(inicio, fin);

        LocalDateTime desdeHora = inicio.atStartOfDay();
        LocalDateTime hastaHora = fin.plusDays(1).atStartOfDay();

        PerspectivaResumen perspectiva;
        List<ResumenEstadoProyeccion> filas;

        switch (contexto.rol()) {
            case ADMINISTRADOR_CENTRAL -> {
                perspectiva = PerspectivaResumen.MONTO_TOTAL;
                filas = comisionRepository.resumenTotal(desdeHora, hastaHora);
            }
            case GERENTE_OFICINA -> {
                perspectiva = PerspectivaResumen.PARTICIPACION_OFICINA;
                filas = comisionRepository.resumenPorGerente(
                        contexto.userId(), BeneficiarioTipo.OFICINA, desdeHora, hastaHora);
            }
            case AGENTE -> {
                perspectiva = PerspectivaResumen.PARTICIPACION_AGENTE;
                filas = comisionRepository.resumenPorAgente(
                        contexto.userId(), BeneficiarioTipo.AGENTE, desdeHora, hastaHora);
            }
            default -> throw new AccesoNoAutorizadoException("Tu rol no tiene acceso a las comisiones");
        }

        Map<EstadoComision, ResumenEstadoProyeccion> porEstado = filas.stream()
                .collect(Collectors.toMap(ResumenEstadoProyeccion::getEstado, Function.identity()));

        List<ResumenEstadoResponse> items = Arrays.stream(EstadoComision.values())
                .map(estado -> {
                    ResumenEstadoProyeccion fila = porEstado.get(estado);
                    return fila == null
                            ? new ResumenEstadoResponse(estado, 0L, BigDecimal.ZERO)
                            : new ResumenEstadoResponse(estado, fila.getCantidad(), fila.getMonto());
                })
                .toList();

        return new ResumenComisionResponse(inicio, fin, perspectiva, items);
    }

    private Comision buscar(UUID id) {
        return comisionRepository.findById(id)
                .orElseThrow(() -> new ComisionNoEncontradaException("La comisión " + id + " no existe"));
    }

    private Specification<Comision> alcance(ContextoUsuario contexto) {
        return switch (contexto.rol()) {
            case ADMINISTRADOR_CENTRAL -> ComisionSpecifications.sinRestriccion();
            case GERENTE_OFICINA -> ComisionSpecifications.porGerente(contexto.userId());
            case AGENTE -> ComisionSpecifications.porAgente(contexto.userId());
            case CLIENTE -> throw new AccesoNoAutorizadoException("Tu rol no tiene acceso a las comisiones");
        };
    }

    private void verificarAcceso(Comision comision, ContextoUsuario contexto) {
        boolean permitido = switch (contexto.rol()) {
            case ADMINISTRADOR_CENTRAL -> true;
            case GERENTE_OFICINA -> contexto.userId().equals(comision.getGerenteOficinaId());
            case AGENTE -> contexto.userId().equals(comision.getAgenteId());
            case CLIENTE -> false;
        };

        if (!permitido) {
            throw new AccesoNoAutorizadoException("No tienes permiso sobre esta comisión");
        }
    }

    private void exigirAdministrador(ContextoUsuario contexto, String mensaje) {
        if (contexto.rol() != RolUsuario.ADMINISTRADOR_CENTRAL) {
            throw new AccesoNoAutorizadoException(mensaje);
        }
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new RangoFechasInvalidoException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
    }
}
