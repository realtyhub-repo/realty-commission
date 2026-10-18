package service.comisiones.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.comisiones.dto.request.CerrarAcuerdoRequest;
import service.comisiones.dto.request.CrearAcuerdoRequest;
import service.comisiones.dto.response.AcuerdoResponse;
import service.comisiones.security.ContextoUsuario;
import service.comisiones.security.UsuarioActual;
import service.comisiones.service.AcuerdoComercialService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comisiones/acuerdos")
@RequiredArgsConstructor
public class AcuerdoComercialController {

    private final AcuerdoComercialService acuerdoService;

    @PostMapping
    public ResponseEntity<AcuerdoResponse> crear(
            @Valid @RequestBody CrearAcuerdoRequest request,
            @UsuarioActual ContextoUsuario contexto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(acuerdoService.crear(request, contexto));
    }

    @GetMapping
    public ResponseEntity<List<AcuerdoResponse>> listar(@UsuarioActual ContextoUsuario contexto) {
        return ResponseEntity.ok(acuerdoService.listar(contexto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcuerdoResponse> obtener(
            @PathVariable UUID id,
            @UsuarioActual ContextoUsuario contexto) {

        return ResponseEntity.ok(acuerdoService.obtener(id, contexto));
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<AcuerdoResponse> cerrar(
            @PathVariable UUID id,
            @Valid @RequestBody CerrarAcuerdoRequest request,
            @UsuarioActual ContextoUsuario contexto) {

        return ResponseEntity.ok(acuerdoService.cerrar(id, request.vigenteHasta(), contexto));
    }
}
