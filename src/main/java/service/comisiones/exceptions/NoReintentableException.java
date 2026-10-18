package service.comisiones.exceptions;

public class NoReintentableException extends RuntimeException {

    public NoReintentableException(String mensaje) {
        super(mensaje);
    }
}
