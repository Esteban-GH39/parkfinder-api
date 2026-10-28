package org.parkfinder.reservas.dominio.excepcion;

/** Los datos de la reserva no son válidos (fecha pasada, fuera de horario, cliente inexistente). */
public class ReservaInvalidaException extends ReservaException {
    public ReservaInvalidaException(String mensaje) {
        super(mensaje, 400, "RESERVA-ERR-400");
    }
}
