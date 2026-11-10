package org.parkfinder.reservas.infraestructura.dto;

import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;

import java.time.LocalDateTime;

public record ReservaConsultaDto(
        Long id,
        Long clienteId,
        Long parqueaderoId,
        Integer cantidad,
        Integer cantidadCarros,
        Integer cantidadMotos,
        LocalDateTime fechaHoraLlegada,
        EstadoReserva estado,
        LocalDateTime creadaEn,
        LocalDateTime expiraEn,
        Double costoReserva,
        LocalDateTime validadaEn
) {
    public static ReservaConsultaDto de(Reserva reserva) {
        return de(reserva, null);
    }

    public static ReservaConsultaDto de(Reserva reserva, Double costoReserva) {
        int total = reserva.cantidad == null ? 1 : reserva.cantidad;
        int motos = reserva.cantidadMotos == null ? 0 : reserva.cantidadMotos;
        return new ReservaConsultaDto(reserva.id, reserva.clienteId, reserva.parqueaderoId,
                total, total - motos, motos, reserva.fechaHoraLlegada, reserva.estado, reserva.creadaEn,
                reserva.expiraEn, costoReserva, reserva.validadaEn);
    }
}
