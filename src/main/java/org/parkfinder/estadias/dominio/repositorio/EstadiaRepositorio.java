package org.parkfinder.estadias.dominio.repositorio;

import org.parkfinder.estadias.dominio.modelo.Estadia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EstadiaRepositorio {

    //METODO REGISTRAR EL INGRESO DE UN VEHÍCULO. Si descontarCupo es true descuenta un cupo de su tipo
    //(ingreso directo); con reserva va en false porque el cupo ya se descontó al reservar.
    //Lanza EstadiaException (409) si no hay cupo de ese tipo.
    Estadia registrarIngreso(Estadia estadia, boolean descontarCupo);

    //METODO BUSCAR EL VEHÍCULO CON ESA PLACA QUE TODAVÍA NO HA SALIDO (en cualquier parqueadero)
    Optional<Estadia> buscarActivaPorPlaca(String placa);

    //METODO LISTAR LOS VEHÍCULOS QUE ESTÁN DENTRO DE UN PARQUEADERO, el que lleva más tiempo primero
    List<Estadia> listarActivas(Long parqueaderoId);

    //METODO REGISTRAR LA SALIDA: guarda la hora, los minutos y el valor cobrado, y devuelve el cupo
    //de su tipo al parqueadero. Lanza EstadiaException (409) si ya había salido.
    Estadia registrarSalida(Long estadiaId, LocalDateTime salidaEn, long minutos, double valorCobrado);
}
