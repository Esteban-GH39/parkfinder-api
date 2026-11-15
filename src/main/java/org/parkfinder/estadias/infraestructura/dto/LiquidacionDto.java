package org.parkfinder.estadias.infraestructura.dto;

import org.parkfinder.estadias.dominio.modelo.Liquidacion;
import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;

import java.time.LocalDateTime;

/**
 * Lo que debe pagar un vehículo. {@code salidaEn} es la hora de corte del cálculo: la salida
 * real si {@code cerrada} es true, o la hora de la consulta si el vehículo sigue adentro.
 */
public record LiquidacionDto(
        Long estadiaId,
        Long parqueaderoId,
        Long reservaId,
        String placa,
        TipoVehiculo tipoVehiculo,
        LocalDateTime ingresoEn,
        LocalDateTime salidaEn,
        boolean cerrada,
        long minutos,
        long diasCompletos,
        long minutosDiurnos,
        long minutosNocturnos,
        Double tarifaHora,
        Double tarifaDia,
        Double tarifaNoche,
        double valorMinuto,
        double valorTotal,
        boolean aplicoTarifaDia,
        boolean aplicoTarifaNoche
) {
    public static LiquidacionDto de(Liquidacion l) {
        var e = l.estadia();
        var c = l.cobro();
        var t = l.tarifas();
        return new LiquidacionDto(e.id, e.parqueaderoId, e.reservaId, e.placa, e.tipoVehiculo,
                e.ingresoEn, l.corte(), l.cerrada(),
                c.minutos(), c.diasCompletos(), c.minutosDiurnos(), c.minutosNocturnos(),
                t.hora(), t.dia(), t.noche(),
                c.valorMinuto(), c.valorTotal(), c.aplicoTarifaDia(), c.aplicoTarifaNoche());
    }
}
