package org.parkfinder.reservas.dominio.repositorio;

import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.modelo.ReservaDetalle;

import java.util.List;

public interface ReservaRepositorio {

    //METODO RETENER CUPO (HU-06). Descuenta un cupo del parqueadero y guarda la reserva en una sola
    //transacción. Lanza ParqueaderoNoEncontradoException, ReservaInvalidaException o SinCupoException.
    Reserva retenerCupo(Reserva reserva);

    //METODO CONSULTAR UNA RESERVA (HU-06). Si la retención venció, la marca EXPIRADA y devuelve el cupo.
    Reserva obtenerPorId(Long id);

    //METODO CONFIRMAR UNA RESERVA RETENIDA CUANDO SU PAGO FUE APROBADO (el cupo ya estaba descontado)
    void confirmar(Long id);

    //METODO VALIDAR EL CÓDIGO DE RESERVA: el cliente llegó. La reserva sigue CONFIRMADA y queda anotada la
    //hora de validación. Lanza ReservaEstadoInvalidoException si no está confirmada o el código ya se usó.
    Reserva validar(Long id);

    //METODO LISTAR LAS RESERVAS CONFIRMADAS DE UN CLIENTE, con su código de reserva
    List<ReservaDetalle> listarConfirmadasPorCliente(Long clienteId);

    //METODO LISTAR LAS RESERVAS RETENIDAS Y SIN VENCER DE UN CLIENTE (pendientes de pago), con la
    //tarifa para poder retomar el pago. Libera antes las retenciones vencidas.
    List<ReservaDetalle> listarPendientesPorCliente(Long clienteId);

    //METODO LISTAR LAS RESERVAS DE LOS PARQUEADEROS DE UN DUEÑO. Libera antes las retenciones vencidas.
    List<ReservaDetalle> listarPorAdmin(int adminId);
}
