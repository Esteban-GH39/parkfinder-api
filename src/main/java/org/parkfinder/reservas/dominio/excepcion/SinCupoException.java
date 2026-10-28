package org.parkfinder.reservas.dominio.excepcion;

/** No hay cupos disponibles: no se genera retención. */
public class SinCupoException extends ReservaException {
    public SinCupoException(Long parqueaderoId) {
        super("El parqueadero " + parqueaderoId + " no tiene cupos disponibles", 409, "RESERVA-ERR-409");
    }
}
