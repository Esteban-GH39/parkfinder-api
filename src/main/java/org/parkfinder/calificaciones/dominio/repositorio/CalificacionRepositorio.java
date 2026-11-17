package org.parkfinder.calificaciones.dominio.repositorio;

import org.parkfinder.calificaciones.dominio.modelo.Calificacion;
import org.parkfinder.calificaciones.dominio.modelo.ResumenCalificaciones;

public interface CalificacionRepositorio {

    //METODO SABER SI EL CLIENTE COMPLETÓ UNA RESERVA EN EL PARQUEADERO (HU-13).
    //Completada = la reserva tiene una estadía con la salida ya registrada.
    boolean clienteCompletoReserva(Long clienteId, Long parqueaderoId);

    //METODO SABER SI EL CLIENTE YA CALIFICÓ EL PARQUEADERO (HU-13)
    boolean yaCalifico(Long clienteId, Long parqueaderoId);

    //METODO SABER SI EXISTEN EL CLIENTE Y EL PARQUEADERO (HU-13)
    boolean existeCliente(Long clienteId);

    boolean existeParqueadero(Long parqueaderoId);

    //METODO GUARDAR LA CALIFICACIÓN (HU-13)
    Calificacion crear(Calificacion calificacion);

    //METODO CONSULTAR EL PROMEDIO Y LOS COMENTARIOS DE UN PARQUEADERO (HU-13)
    ResumenCalificaciones resumenPorParqueadero(Long parqueaderoId);
}
