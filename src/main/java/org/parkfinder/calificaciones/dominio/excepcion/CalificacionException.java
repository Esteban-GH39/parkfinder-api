package org.parkfinder.calificaciones.dominio.excepcion;

/** Base de los errores de negocio de la calificación (HU-13); cada uno trae su código HTTP. */
public abstract class CalificacionException extends RuntimeException {

    private final int status;
    private final String codigo;

    protected CalificacionException(String mensaje, int status, String codigo) {
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
