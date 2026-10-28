package org.parkfinder.reservas.dominio.excepcion;

/** Base de los errores de negocio de la reserva (HU-06); cada uno trae su código HTTP. */
public abstract class ReservaException extends RuntimeException {

    private final int status;
    private final String codigo;

    protected ReservaException(String mensaje, int status, String codigo) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
    }

    public int getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }
}
