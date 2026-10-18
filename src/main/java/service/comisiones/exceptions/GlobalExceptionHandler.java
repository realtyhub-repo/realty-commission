package service.comisiones.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import service.comisiones.dto.internal.ErrorResponse;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ContextoUsuarioInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleContextoInvalido(ContextoUsuarioInvalidoException ex) {
        return construir(HttpStatus.UNAUTHORIZED, "CONTEXTO_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<ErrorResponse> handleAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
        return construir(HttpStatus.FORBIDDEN, "ACCESO_NO_AUTORIZADO", ex.getMessage());
    }

    @ExceptionHandler(ComisionNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleComisionNoEncontrada(ComisionNoEncontradaException ex) {
        return construir(HttpStatus.NOT_FOUND, "COMISION_NO_ENCONTRADA", ex.getMessage());
    }

    @ExceptionHandler(AcuerdoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleAcuerdoNoEncontrado(AcuerdoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, "ACUERDO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(AcuerdoInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleAcuerdoInvalido(AcuerdoInvalidoException ex) {
        return construir(HttpStatus.BAD_REQUEST, "ACUERDO_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(AcuerdoSolapadoException.class)
    public ResponseEntity<ErrorResponse> handleAcuerdoSolapado(AcuerdoSolapadoException ex) {
        return construir(HttpStatus.CONFLICT, "ACUERDO_SOLAPADO", ex.getMessage());
    }

    @ExceptionHandler(TransicionInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleTransicionInvalida(TransicionInvalidaException ex) {
        return construir(HttpStatus.CONFLICT, "TRANSICION_INVALIDA", ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConflictoConcurrente(ObjectOptimisticLockingFailureException ex) {
        return construir(HttpStatus.CONFLICT, "CONFLICTO_CONCURRENTE",
                "La comisión fue modificada por otra operación. Vuelve a consultarla");
    }

    @ExceptionHandler(RangoFechasInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleRangoInvalido(RangoFechasInvalidoException ex) {
        return construir(HttpStatus.BAD_REQUEST, "RANGO_FECHAS_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleServicioNoDisponible(ServicioNoDisponibleException ex) {
        return construir(HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_NO_DISPONIBLE", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Solicitud inválida");
        return construir(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", mensaje);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleSolicitudMalFormada(Exception ex) {
        return construir(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                "La solicitud tiene parámetros faltantes o con formato inválido");
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String codigo, String mensaje) {
        ErrorResponse error = new ErrorResponse(codigo, mensaje, status.value(), LocalDateTime.now());
        return ResponseEntity.status(status).body(error);
    }
}
