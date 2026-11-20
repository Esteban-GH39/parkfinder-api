package org.parkfinder.billetera.dominio.modelo;

/** Por qué cambió el saldo: RECARGA y REEMBOLSO lo suben; PAGO_RESERVA lo baja. */
public enum TipoMovimiento {
    RECARGA,
    PAGO_RESERVA,
    REEMBOLSO
}
