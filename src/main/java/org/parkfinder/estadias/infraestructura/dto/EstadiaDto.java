package org.parkfinder.estadias.infraestructura.dto;

import org.parkfinder.estadias.dominio.modelo.Estadia;
import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;

import java.time.LocalDateTime;

public record EstadiaDto(
        Long id,
        Long parqueaderoId,
        Long reservaId,
        String placa,
        TipoVehiculo tipoVehiculo,
        LocalDateTime ingresoEn,
        LocalDateTime salidaEn,
        Long minutos,
        Double valorCobrado
) {
    public static EstadiaDto de(Estadia e) {
        return new EstadiaDto(e.id, e.parqueaderoId, e.reservaId, e.placa, e.tipoVehiculo,
                e.ingresoEn, e.salidaEn, e.minutos, e.valorCobrado);
    }
}
