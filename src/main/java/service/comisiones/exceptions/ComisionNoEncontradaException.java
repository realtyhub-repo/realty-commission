package service.comisiones.exceptions;

public class ComisionNoEncontradaException extends RuntimeException {

    public ComisionNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
