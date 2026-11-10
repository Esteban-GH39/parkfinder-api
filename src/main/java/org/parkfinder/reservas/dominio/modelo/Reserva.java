package org.parkfinder.reservas.dominio.modelo;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class Reserva {
    public Long id;
    public Long clienteId;
    public Long parqueaderoId;
    /** Cupos que pidió el cliente en esta reserva; null en reservas viejas equivale a 1. */
    /** Total de cupos de la reserva (carros + motos); null en reservas viejas equivale a 1. */
    public Integer cantidad;
    /** Cuántos de esos cupos son de moto (el resto son de carro); null equivale a 0. */
    public Integer cantidadMotos;
    /** Cuándo el dueño validó el código de reserva; null mientras no se haya usado. */
    public LocalDateTime validadaEn;
    public LocalDateTime fechaHoraLlegada;
    public EstadoReserva estado;
    public LocalDateTime creadaEn;
    public LocalDateTime expiraEn;
}
