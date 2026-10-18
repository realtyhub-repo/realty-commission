package service.comisiones.exceptions;

public class AgenteNoEncontradoException extends NoReintentableException {

    public AgenteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
