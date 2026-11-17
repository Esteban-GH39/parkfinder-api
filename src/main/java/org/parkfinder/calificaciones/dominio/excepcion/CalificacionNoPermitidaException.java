package org.parkfinder.calificaciones.dominio.excepcion;

/** El cliente aún no completó una reserva en ese parqueadero. */
public class CalificacionNoPermitidaException extends CalificacionException {
    public CalificacionNoPermitidaException() {
        super("Solo puedes calificar un parqueadero después de haber completado una reserva en él",
                403, "CALIFICACION-ERR-403");
    }
}
