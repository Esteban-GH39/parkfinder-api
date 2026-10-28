package org.parkfinder.reservas.dominio.modelo;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Una reserva con los nombres que necesita el dueño del parqueadero para verla:
 * qué parqueadero y qué cliente.
 */
@Builder
public class ReservaDetalle {
    public Long id;
    public Long parqueaderoId;
    public String parqueaderoNombre;
    public String clienteNombre;
    public String clienteCorreo;
    public LocalDateTime fechaHoraLlegada;
    public EstadoReserva estado;
    public LocalDateTime creadaEn;
    public LocalDateTime expiraEn;
}
