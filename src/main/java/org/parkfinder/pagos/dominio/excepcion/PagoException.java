package org.parkfinder.pagos.dominio.excepcion;

/** Base de los errores de negocio del pago (HU-07); cada uno trae su código HTTP. */
public abstract class PagoException extends RuntimeException {

    private final int status;
    private final String codigo;

    protected PagoException(String mensaje, int status, String codigo) {
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
