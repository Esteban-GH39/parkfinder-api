package org.parkfinder.billetera.dominio.excepcion;

/** Monto fuera de los límites o medio de pago que no sirve para recargar. */
public class RecargaInvalidaException extends BilleteraException {
    public RecargaInvalidaException(String mensaje) {
        super(mensaje, 400, "BILLETERA-ERR-400");
    }
}
