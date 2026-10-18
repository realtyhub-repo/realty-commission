package service.comisiones.enums;

public enum EstadoComision {
    PENDIENTE,
    APROBADA,
    PAGADA,
    ANULADA,
    REVERTIDA;

    public boolean puedePasarA(EstadoComision destino) {
        return switch (this) {
            case PENDIENTE -> destino == APROBADA || destino == ANULADA;
            case APROBADA -> destino == PAGADA || destino == ANULADA;
            case PAGADA -> destino == REVERTIDA;
            case ANULADA, REVERTIDA -> false;
        };
    }
}
