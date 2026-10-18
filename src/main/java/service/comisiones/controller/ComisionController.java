package service.comisiones.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.comisiones.dto.request.PagarComisionRequest;
import service.comisiones.dto.request.RevertirComisionRequest;
import service.comisiones.dto.response.ComisionDetalleResponse;
import service.comisiones.dto.response.ComisionResponse;
import service.comisiones.dto.response.PaginaResponse;
import service.comisiones.dto.response.ResumenComisionResponse;
import service.comisiones.enums.EstadoComision;
import service.comisiones.security.ContextoUsuario;
import service.comisiones.security.UsuarioActual;
import service.comisiones.service.ComisionService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/comisiones")
@RequiredArgsConstructor
public class ComisionController {

    private final ComisionService comisionService;

    @GetMapping
    public ResponseEntity<PaginaResponse<ComisionResponse>> listar(
            @UsuarioActual ContextoUsuario contexto,
            @RequestParam(required = false) EstadoComision estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {

        return ResponseEntity.ok(comisionService.listar(contexto, estado, desde, hasta, pagina, tamanio));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenComisionResponse> resumen(
            @UsuarioActual ContextoUsuario contexto,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        return ResponseEntity.ok(comisionService.resumen(contexto, desde, hasta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComisionDetalleResponse> obtener(
            @PathVariable UUID id,
            @UsuarioActual ContextoUsuario contexto) {

        return ResponseEntity.ok(comisionService.obtener(id, contexto));
    }

    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<ComisionResponse> aprobar(
            @PathVariable UUID id,
            @UsuarioActual ContextoUsuario contexto) {

        return ResponseEntity.ok(comisionService.aprobar(id, contexto));
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<ComisionResponse> pagar(
            @PathVariable UUID id,
            @Valid @RequestBody PagarComisionRequest request,
            @UsuarioActual ContextoUsuario contexto) {

        return ResponseEntity.ok(comisionService.pagar(id, request.referenciaPago().trim(), contexto));
    }

    @PatchMapping("/{id}/revertir")
    public ResponseEntity<ComisionResponse> revertir(
            @PathVariable UUID id,
            @Valid @RequestBody RevertirComisionRequest request,
            @UsuarioActual ContextoUsuario contexto) {

        return ResponseEntity.ok(comisionService.revertir(id, request.motivo().trim(), contexto));
    }
}
