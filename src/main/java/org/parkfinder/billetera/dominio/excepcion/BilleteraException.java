package org.parkfinder.billetera.dominio.excepcion;

/** Base de los errores de negocio de la billetera; cada uno trae su código HTTP. */
public abstract class BilleteraException extends RuntimeException {

    private final int status;
    private final String codigo;

    protected BilleteraException(String mensaje, int status, String codigo) {
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
