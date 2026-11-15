package org.parkfinder.estadias.dominio.modelo;

import lombok.Builder;
import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;

import java.time.LocalDateTime;

/**
 * El paso de un vehículo por el parqueadero: desde que el administrador registra su placa al
 * ingresar (ingreso directo o con código de reserva) hasta que libera el cupo y se le cobra.
 */
@Builder
public class Estadia {
    public Long id;
    public Long parqueaderoId;
    /** Reserva con la que ingresó; null si fue un ingreso directo. */
    public Long reservaId;
    /** Placa normalizada: mayúsculas, sin espacios ni guiones. */
    public String placa;
    public TipoVehiculo tipoVehiculo;
    /** Fecha y hora de ingreso, la registra el sistema. */
    public LocalDateTime ingresoEn;
    /** Fecha y hora de salida; null mientras el vehículo sigue en el parqueadero. */
    public LocalDateTime salidaEn;
    /** Minutos cobrados; null mientras no ha salido. */
    public Long minutos;
    /** Valor cobrado al salir; null mientras no ha salido. */
    public Double valorCobrado;
}
