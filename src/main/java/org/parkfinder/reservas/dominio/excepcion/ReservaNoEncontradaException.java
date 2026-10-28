package org.parkfinder.reservas.dominio.excepcion;

public class ReservaNoEncontradaException extends ReservaException {
    public ReservaNoEncontradaException(Long id) {
        super("No existe una reserva con id " + id, 404, "RESERVA-ERR-404");
    }
}
