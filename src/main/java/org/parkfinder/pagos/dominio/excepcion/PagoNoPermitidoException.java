package org.parkfinder.pagos.dominio.excepcion;

/** La reserva no está en condiciones de pagarse (retención vencida o ya pagada). */
public class PagoNoPermitidoException extends PagoException {
    public PagoNoPermitidoException(String mensaje) {
        super(mensaje, 409, "PAGO-ERR-409");
    }
}
