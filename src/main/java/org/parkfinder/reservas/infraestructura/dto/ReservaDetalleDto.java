package org.parkfinder.reservas.infraestructura.dto;

import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.ReservaDetalle;

import java.time.LocalDateTime;

public record ReservaDetalleDto(
        Long id,
        Long parqueaderoId,
        String parqueaderoNombre,
        String clienteNombre,
        String clienteCorreo,
        LocalDateTime fechaHoraLlegada,
        EstadoReserva estado,
        LocalDateTime creadaEn,
        LocalDateTime expiraEn
) {
    public static ReservaDetalleDto de(ReservaDetalle r) {
        return new ReservaDetalleDto(r.id, r.parqueaderoId, r.parqueaderoNombre, r.clienteNombre,
                r.clienteCorreo, r.fechaHoraLlegada, r.estado, r.creadaEn, r.expiraEn);
    }
}
