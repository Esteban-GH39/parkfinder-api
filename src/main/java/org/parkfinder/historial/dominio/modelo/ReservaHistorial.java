package org.parkfinder.historial.dominio.modelo;

import org.parkfinder.reservas.dominio.modelo.EstadoReserva;

import java.time.LocalDateTime;

/**
 * Una reserva dentro del historial (HU-24), con lo que se pagó por ella.
 * {@code montoPagado} es 0 si la reserva no tiene un pago aprobado.
 */
public record ReservaHistorial(
        Long id,
        Long parqueaderoId,
        String parqueaderoNombre,
        String clienteNombre,
        String clienteCorreo,
        Integer cantidad,
        Integer cantidadMotos,
        Long codigoReserva,
        EstadoReserva estado,
        LocalDateTime creadaEn,
        LocalDateTime expiraEn,
        LocalDateTime fechaHoraLlegada,
        LocalDateTime validadaEn,
        double montoPagado
) {
    /** La misma reserva con otro estado (para mostrar EXPIRADA una retención ya vencida). */
    public ReservaHistorial conEstado(EstadoReserva nuevo) {
        return new ReservaHistorial(id, parqueaderoId, parqueaderoNombre, clienteNombre, clienteCorreo, cantidad,
                cantidadMotos, codigoReserva, nuevo, creadaEn, expiraEn, fechaHoraLlegada, validadaEn, montoPagado);
    }
}
