package org.parkfinder.reservas.dominio.repositorio;

import org.parkfinder.reservas.dominio.modelo.Reserva;

public interface ReservaRepositorio {

    //METODO RETENER CUPO (HU-06). Descuenta un cupo del parqueadero y guarda la reserva en una sola
    //transacción. Lanza ParqueaderoNoEncontradoException, ReservaInvalidaException o SinCupoException.
    Reserva retenerCupo(Reserva reserva);

    //METODO CONSULTAR UNA RESERVA (HU-06). Si la retención venció, la marca EXPIRADA y devuelve el cupo.
    Reserva obtenerPorId(Long id);
}
