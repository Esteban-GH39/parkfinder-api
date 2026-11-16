package org.parkfinder.historial.dominio.repositorio;

import org.parkfinder.historial.dominio.modelo.CobroEstadia;
import org.parkfinder.historial.dominio.modelo.ReservaHistorial;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface HistorialRepositorio {

    //METODO LISTAR LAS RESERVAS CREADAS EN [desde, hasta) DE ESOS PARQUEADEROS (HU-24), la más reciente
    //primero, con el cliente, el código de reserva y el monto del pago aprobado (0 si no se pagó).
    //nombres trae el nombre de cada parqueadero por su id.
    List<ReservaHistorial> listarReservas(Map<Long, String> nombres, LocalDateTime desde, LocalDateTime hasta);

    //METODO LISTAR LOS VEHÍCULOS QUE SALIERON EN [desde, hasta) DE ESOS PARQUEADEROS (HU-24), con lo
    //que se les cobró, el más reciente primero.
    List<CobroEstadia> listarCobros(Map<Long, String> nombres, LocalDateTime desde, LocalDateTime hasta);
}
