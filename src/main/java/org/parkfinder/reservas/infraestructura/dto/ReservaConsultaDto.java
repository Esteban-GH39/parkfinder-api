package org.parkfinder.reservas.infraestructura.dto;

import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;

import java.time.LocalDateTime;

public record ReservaConsultaDto(
        Long id,
        Long clienteId,
        Long parqueaderoId,
        LocalDateTime fechaHoraLlegada,
        EstadoReserva estado,
        LocalDateTime creadaEn,
        LocalDateTime expiraEn
) {
    public static ReservaConsultaDto de(Reserva reserva) {
        return new ReservaConsultaDto(reserva.id, reserva.clienteId, reserva.parqueaderoId,
                reserva.fechaHoraLlegada, reserva.estado, reserva.creadaEn, reserva.expiraEn);
    }
}
