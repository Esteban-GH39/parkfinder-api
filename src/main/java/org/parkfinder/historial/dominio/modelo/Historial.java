package org.parkfinder.historial.dominio.modelo;

import java.time.LocalDate;
import java.util.List;

/**
 * Historial de reservas e ingresos de los parqueaderos de un dueño en un periodo (HU-24).
 * Las reservas cuentan por su fecha de creación y los cobros de vehículos por su fecha de salida.
 * {@code parqueaderoId} es null cuando se consultan todos los parqueaderos del dueño.
 */
public record Historial(
        Periodo periodo,
        LocalDate fecha,
        LocalDate desde,
        LocalDate hasta,
        Long parqueaderoId,
        double ingresosReservas,
        double ingresosEstadias,
        double ingresosTotales,
        int totalReservas,
        int reservasPagadas,
        int vehiculosCobrados,
        List<IngresoDia> desglose,
        List<ReservaHistorial> reservas,
        List<CobroEstadia> cobros
) {
}
