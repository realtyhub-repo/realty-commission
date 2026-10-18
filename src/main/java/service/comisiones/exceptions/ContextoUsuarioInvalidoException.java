package service.comisiones.exceptions;

public class ContextoUsuarioInvalidoException extends RuntimeException {

    public ContextoUsuarioInvalidoException(String mensaje) {
        super(mensaje);
    }
}
