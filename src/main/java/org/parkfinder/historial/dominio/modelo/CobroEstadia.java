package org.parkfinder.historial.dominio.modelo;

import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;

import java.time.LocalDateTime;

/** Un vehículo que ya salió y pagó su tiempo en el parqueadero (HU-24). */
public record CobroEstadia(
        Long id,
        Long parqueaderoId,
        String parqueaderoNombre,
        Long reservaId,
        String placa,
        TipoVehiculo tipoVehiculo,
        LocalDateTime ingresoEn,
        LocalDateTime salidaEn,
        Long minutos,
        double valorCobrado
) {
}
