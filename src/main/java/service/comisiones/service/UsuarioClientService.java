package service.comisiones.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import service.comisiones.dto.response.AgentePerfilInternalResponse;
import service.comisiones.dto.response.OficinaInternalResponse;
import service.comisiones.exceptions.AgenteNoEncontradoException;
import service.comisiones.exceptions.OficinaNoEncontradaException;
import service.comisiones.exceptions.ServicioNoDisponibleException;

import java.util.UUID;

@Service
@Slf4j
public class UsuarioClientService {

    private final RestClient usuarioClient;

    public UsuarioClientService(@Qualifier("usuarioRestClient") RestClient usuarioClient) {
        this.usuarioClient = usuarioClient;
    }

    public AgentePerfilInternalResponse buscarAgentePorUsuario(UUID usuarioId) {
        try {
            AgentePerfilInternalResponse respuesta = usuarioClient.get()
                    .uri("/agentes/usuario/{usuarioId}", usuarioId)
                    .retrieve()
                    .body(AgentePerfilInternalResponse.class);

            if (respuesta == null) {
                throw new ServicioNoDisponibleException("User Service respondió sin datos");
            }
            return respuesta;

        } catch (HttpClientErrorException.NotFound e) {
            throw new AgenteNoEncontradoException("No existe un agente para el usuario " + usuarioId);
        } catch (RestClientException e) {
            log.error("No se pudo consultar el agente del usuario {}: {}", usuarioId, e.getMessage());
            throw new ServicioNoDisponibleException("El servicio de usuarios no está disponible");
        }
    }

    public OficinaInternalResponse buscarOficina(UUID oficinaId) {
        try {
            OficinaInternalResponse respuesta = usuarioClient.get()
                    .uri("/oficinas/{id}", oficinaId)
                    .retrieve()
                    .body(OficinaInternalResponse.class);

            if (respuesta == null) {
                throw new ServicioNoDisponibleException("User Service respondió sin datos");
            }
            return respuesta;

        } catch (HttpClientErrorException.NotFound e) {
            throw new OficinaNoEncontradaException("La oficina " + oficinaId + " no existe");
        } catch (RestClientException e) {
            log.error("No se pudo consultar la oficina {}: {}", oficinaId, e.getMessage());
            throw new ServicioNoDisponibleException("El servicio de usuarios no está disponible");
        }
    }
}
