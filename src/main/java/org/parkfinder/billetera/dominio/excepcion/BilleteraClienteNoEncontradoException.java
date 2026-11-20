package org.parkfinder.billetera.dominio.excepcion;

public class BilleteraClienteNoEncontradoException extends BilleteraException {
    public BilleteraClienteNoEncontradoException(Long clienteId) {
        super("El cliente " + clienteId + " no existe", 404, "BILLETERA-ERR-404");
    }
}
