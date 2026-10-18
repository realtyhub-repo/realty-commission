package service.comisiones.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.comisiones.dto.request.CrearAcuerdoRequest;
import service.comisiones.dto.request.ReglaRequest;
import service.comisiones.dto.response.AcuerdoResponse;
import service.comisiones.dto.response.ReglaResponse;
import service.comisiones.entity.AcuerdoComercial;
import service.comisiones.entity.ReglaComision;
import service.comisiones.enums.BaseCalculo;
import service.comisiones.enums.RolUsuario;
import service.comisiones.enums.TipoContrato;
import service.comisiones.exceptions.AccesoNoAutorizadoException;
import service.comisiones.exceptions.AcuerdoInvalidoException;
import service.comisiones.exceptions.AcuerdoNoEncontradoException;
import service.comisiones.exceptions.AcuerdoSolapadoException;
import service.comisiones.repository.AcuerdoComercialRepository;
import service.comisiones.security.ContextoUsuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AcuerdoComercialService {

    private static final Set<TipoContrato> TIPOS_OBLIGATORIOS =
            EnumSet.of(TipoContrato.COMPRAVENTA, TipoContrato.ALQUILER);

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final AcuerdoComercialRepository acuerdoRepository;

    @Transactional
    public AcuerdoResponse crear(CrearAcuerdoRequest request, ContextoUsuario contexto) {
        exigirAdministrador(contexto);
        validar(request);
        verificarSinSolape(request.vigenteDesde(), request.vigenteHasta());

        AcuerdoComercial acuerdo = AcuerdoComercial.builder()
                .nombre(request.nombre().trim())
                .vigenteDesde(request.vigenteDesde())
                .vigenteHasta(request.vigenteHasta())
                .creadoPor(contexto.userId())
                .build();

        for (ReglaRequest regla : request.reglas()) {
            acuerdo.agregarRegla(ReglaComision.builder()
                    .tipoContrato(regla.tipoContrato())
                    .baseCalculo(regla.baseCalculo())
                    .valor(regla.valor())
                    .porcentajeAgente(regla.porcentajeAgente())
                    .porcentajeOficina(regla.porcentajeOficina())
                    .porcentajeCentral(regla.porcentajeCentral())
                    .build());
        }

        try {
            return aRespuesta(acuerdoRepository.saveAndFlush(acuerdo));
        } catch (DataIntegrityViolationException e) {
            throw new AcuerdoSolapadoException("La vigencia del acuerdo se cruza con otro acuerdo existente");
        }
    }

    @Transactional(readOnly = true)
    public List<AcuerdoResponse> listar(ContextoUsuario contexto) {
        exigirLectura(contexto);

        return acuerdoRepository.findAllByOrderByVigenteDesdeDesc().stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcuerdoResponse obtener(UUID id, ContextoUsuario contexto) {
        exigirLectura(contexto);

        return aRespuesta(buscar(id));
    }

    @Transactional
    public AcuerdoResponse cerrar(UUID id, LocalDate vigenteHasta, ContextoUsuario contexto) {
        exigirAdministrador(contexto);

        AcuerdoComercial acuerdo = buscar(id);

        if (vigenteHasta.isBefore(acuerdo.getVigenteDesde())) {
            throw new AcuerdoInvalidoException("La fecha de cierre no puede ser anterior al inicio de la vigencia");
        }
        if (acuerdo.getVigenteHasta() != null && vigenteHasta.isAfter(acuerdo.getVigenteHasta())) {
            throw new AcuerdoInvalidoException("Solo se puede acortar la vigencia de un acuerdo, no ampliarla");
        }

        acuerdo.setVigenteHasta(vigenteHasta);

        return aRespuesta(acuerdoRepository.save(acuerdo));
    }

    private AcuerdoComercial buscar(UUID id) {
        return acuerdoRepository.findById(id)
                .orElseThrow(() -> new AcuerdoNoEncontradoException("El acuerdo " + id + " no existe"));
    }

    private void validar(CrearAcuerdoRequest request) {
        if (request.vigenteHasta() != null && request.vigenteHasta().isBefore(request.vigenteDesde())) {
            throw new AcuerdoInvalidoException("vigenteHasta no puede ser anterior a vigenteDesde");
        }

        Set<TipoContrato> vistos = EnumSet.noneOf(TipoContrato.class);

        for (ReglaRequest regla : request.reglas()) {
            if (!vistos.add(regla.tipoContrato())) {
                throw new AcuerdoInvalidoException("Hay más de una regla para " + regla.tipoContrato());
            }
            if (regla.baseCalculo() == BaseCalculo.MESES_CANON && regla.tipoContrato() != TipoContrato.ALQUILER) {
                throw new AcuerdoInvalidoException("MESES_CANON solo aplica a contratos de ALQUILER");
            }
            if (regla.baseCalculo() == BaseCalculo.PORCENTAJE_PRECIO && regla.valor().compareTo(CIEN) > 0) {
                throw new AcuerdoInvalidoException("El porcentaje sobre el precio no puede superar 100");
            }

            BigDecimal suma = regla.porcentajeAgente()
                    .add(regla.porcentajeOficina())
                    .add(regla.porcentajeCentral());

            if (suma.compareTo(CIEN) != 0) {
                throw new AcuerdoInvalidoException(
                        "El reparto de " + regla.tipoContrato() + " debe sumar 100 y suma " + suma.stripTrailingZeros().toPlainString());
            }
        }

        for (TipoContrato obligatorio : TIPOS_OBLIGATORIOS) {
            if (!vistos.contains(obligatorio)) {
                throw new AcuerdoInvalidoException("Falta la regla para " + obligatorio);
            }
        }
    }

    private void verificarSinSolape(LocalDate desde, LocalDate hasta) {
        long solapados = hasta == null
                ? acuerdoRepository.contarSolapadosDesde(desde)
                : acuerdoRepository.contarSolapadosConRango(desde, hasta);

        if (solapados > 0) {
            throw new AcuerdoSolapadoException(
                    "La vigencia se cruza con otro acuerdo. Cierra el acuerdo vigente antes de crear uno nuevo");
        }
    }

    private void exigirAdministrador(ContextoUsuario contexto) {
        if (contexto.rol() != RolUsuario.ADMINISTRADOR_CENTRAL) {
            throw new AccesoNoAutorizadoException("Solo el administrador central gestiona los acuerdos comerciales");
        }
    }

    private void exigirLectura(ContextoUsuario contexto) {
        if (contexto.rol() != RolUsuario.ADMINISTRADOR_CENTRAL && contexto.rol() != RolUsuario.GERENTE_OFICINA) {
            throw new AccesoNoAutorizadoException("No tienes permiso para consultar los acuerdos comerciales");
        }
    }

    private AcuerdoResponse aRespuesta(AcuerdoComercial acuerdo) {
        List<ReglaResponse> reglas = acuerdo.getReglas().stream()
                .map(r -> new ReglaResponse(
                        r.getId(),
                        r.getTipoContrato(),
                        r.getBaseCalculo(),
                        r.getValor(),
                        r.getPorcentajeAgente(),
                        r.getPorcentajeOficina(),
                        r.getPorcentajeCentral()))
                .toList();

        return new AcuerdoResponse(
                acuerdo.getId(),
                acuerdo.getNombre(),
                acuerdo.getVigenteDesde(),
                acuerdo.getVigenteHasta(),
                acuerdo.getCreadoPor(),
                acuerdo.getCreatedAt(),
                reglas);
    }
}
