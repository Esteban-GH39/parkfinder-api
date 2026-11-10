package org.parkfinder.reservas.dominio.excepcion;

import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;

/** No hay cupos disponibles: no se genera retención. */
public class SinCupoException extends ReservaException {
    public SinCupoException(Long parqueaderoId, TipoVehiculo tipo, int pedidos, int disponibles) {
        super(mensaje(parqueaderoId, tipo, pedidos, disponibles), 409, "RESERVA-ERR-409");
    }

    public SinCupoException(Long parqueaderoId) {
        super("El parqueadero " + parqueaderoId + " no tiene cupos disponibles", 409, "RESERVA-ERR-409");
    }

    private static String mensaje(Long parqueaderoId, TipoVehiculo tipo, int pedidos, int disponibles) {
        String clase = tipo == TipoVehiculo.MOTO ? "de moto" : "de carro";
        if (disponibles <= 0) {
            return "El parqueadero " + parqueaderoId + " no tiene cupos " + clase + " disponibles";
        }
        return "El parqueadero " + parqueaderoId + " solo tiene " + disponibles + " cupo(s) " + clase
                + " disponible(s) y pediste " + pedidos;
    }
}
