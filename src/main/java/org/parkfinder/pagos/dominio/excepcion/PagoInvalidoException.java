package org.parkfinder.pagos.dominio.excepcion;

/** Faltan datos del medio de pago. La pasarela no llega a procesar nada. */
public class PagoInvalidoException extends PagoException {
    public PagoInvalidoException(String mensaje) {
        super(mensaje, 400, "PAGO-ERR-400");
    }
}
