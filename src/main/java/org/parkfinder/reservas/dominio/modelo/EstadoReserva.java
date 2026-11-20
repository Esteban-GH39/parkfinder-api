package org.parkfinder.reservas.dominio.modelo;

/** Ciclo de vida de una reserva (HU-06). CANCELADA: se agotaron los intentos de pago (HU-08). */
public enum EstadoReserva {
    RETENIDA,
    CONFIRMADA,
    EXPIRADA,
    CANCELADA
}
