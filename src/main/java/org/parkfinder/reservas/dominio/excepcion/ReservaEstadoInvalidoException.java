package org.parkfinder.reservas.dominio.excepcion;

/** La reserva no está en un estado que permita la operación (por ejemplo, validar un código ya usado). */
public class ReservaEstadoInvalidoException extends ReservaException {
    public ReservaEstadoInvalidoException(String mensaje) {
        super(mensaje, 409, "RESERVA-ERR-409");
    }
}
