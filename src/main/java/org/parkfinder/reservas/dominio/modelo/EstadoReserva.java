package org.parkfinder.reservas.dominio.modelo;

/** Ciclo de vida de una reserva (HU-06). La confirmación llega con HU-08. */
public enum EstadoReserva {
    RETENIDA,
    CONFIRMADA,
    EXPIRADA
}
