package org.parkfinder.reservas.dominio.modelo;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class Reserva {
    public Long id;
    public Long clienteId;
    public Long parqueaderoId;
    public LocalDateTime fechaHoraLlegada;
    public EstadoReserva estado;
    public LocalDateTime creadaEn;
    public LocalDateTime expiraEn;
}
