package org.parkfinder.billetera.dominio.repositorio;

import org.parkfinder.billetera.dominio.modelo.Movimiento;
import org.parkfinder.billetera.dominio.modelo.TipoMovimiento;

import java.util.List;

public interface BilleteraRepositorio {

    //MÉTODO SALDO ACTUAL DE UN CLIENTE (0 si nunca ha usado la billetera)
    long saldoDe(Long clienteId);

    //MÉTODO SUBIR EL SALDO (recarga o reembolso) y dejar el movimiento anotado, en una sola transacción
    Movimiento acreditar(Long clienteId, TipoMovimiento tipo, long monto, Long reservaId, String referencia, String detalle);

    //MÉTODO BAJAR EL SALDO para pagar una reserva. Lanza SaldoInsuficienteException si no alcanza.
    Movimiento debitar(Long clienteId, long monto, Long reservaId, String referencia, String detalle);

    //MÉTODO ÚLTIMOS MOVIMIENTOS, el más reciente primero
    List<Movimiento> movimientos(Long clienteId, int limite);

    //MÉTODO SABER SI YA HAY UN MOVIMIENTO DE ESE TIPO PARA LA RESERVA (evita reembolsar dos veces)
    boolean existeMovimiento(Long reservaId, TipoMovimiento tipo);
}
