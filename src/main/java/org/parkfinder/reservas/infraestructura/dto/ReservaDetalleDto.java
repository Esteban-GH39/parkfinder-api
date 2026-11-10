package org.parkfinder.reservas.infraestructura.dto;

import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.ReservaDetalle;

import java.time.LocalDateTime;

public record ReservaDetalleDto(
        Long id,
        Long parqueaderoId,
        String parqueaderoNombre,
        Double tarifaHora,
        Double tarifaHoraMoto,
        Integer cantidad,
        Integer cantidadCarros,
        Integer cantidadMotos,
        String clienteNombre,
        String clienteCorreo,
        LocalDateTime fechaHoraLlegada,
        EstadoReserva estado,
        LocalDateTime creadaEn,
        LocalDateTime expiraEn,
        Long codigoReserva,
        Double costoReserva,
        LocalDateTime validadaEn
) {
    public static ReservaDetalleDto de(ReservaDetalle r) {
        return de(r, null);
    }

    public static ReservaDetalleDto de(ReservaDetalle r, Double costoReserva) {
        int total = r.cantidad == null ? 1 : r.cantidad;
        int motos = r.cantidadMotos == null ? 0 : r.cantidadMotos;
        return new ReservaDetalleDto(r.id, r.parqueaderoId, r.parqueaderoNombre, r.tarifaHora, r.tarifaHoraMoto,
                total, total - motos, motos, r.clienteNombre, r.clienteCorreo, r.fechaHoraLlegada, r.estado,
                r.creadaEn, r.expiraEn, r.codigoReserva, costoReserva, r.validadaEn);
    }
}
