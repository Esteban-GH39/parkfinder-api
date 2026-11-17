package org.parkfinder.calificaciones.dominio.excepcion;

/** Un cliente califica una sola vez cada parqueadero. */
public class CalificacionDuplicadaException extends CalificacionException {
    public CalificacionDuplicadaException() {
        super("Ya calificaste este parqueadero", 409, "CALIFICACION-ERR-409");
    }
}
