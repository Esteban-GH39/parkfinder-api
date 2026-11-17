package org.parkfinder.calificaciones.dominio.excepcion;

/** Datos que no cumplen las reglas (estrellas fuera de 1 a 5, cliente o parqueadero inexistente). */
public class CalificacionInvalidaException extends CalificacionException {
    public CalificacionInvalidaException(String mensaje) {
        super(mensaje, 400, "CALIFICACION-ERR-400");
    }
}
