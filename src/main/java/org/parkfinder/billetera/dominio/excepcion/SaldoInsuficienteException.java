package org.parkfinder.billetera.dominio.excepcion;

/** El saldo no alcanza para pagar. No cuenta como intento de pago rechazado. */
public class SaldoInsuficienteException extends BilleteraException {
    public SaldoInsuficienteException(String mensaje) {
        super(mensaje, 409, "BILLETERA-ERR-409");
    }
}
