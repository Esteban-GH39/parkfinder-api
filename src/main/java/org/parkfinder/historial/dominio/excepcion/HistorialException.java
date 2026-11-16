package org.parkfinder.historial.dominio.excepcion;

/** Errores de la consulta del historial de reservas e ingresos (HU-24); cada uno trae su código HTTP. */
public class HistorialException extends RuntimeException {

    private final int status;
    private final String codigo;

    protected HistorialException(String mensaje, int status, String codigo) {
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

    /** Periodo o fecha con formato inválido. */
    public static HistorialException invalida(String mensaje) {
        return new HistorialException(mensaje, 400, "HISTORIAL-ERR-400");
    }

    /** El parqueadero no pertenece al administrador que consulta. */
    public static HistorialException noPermitida(String mensaje) {
        return new HistorialException(mensaje, 403, "HISTORIAL-ERR-403");
    }
}
