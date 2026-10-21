package org.parkfinder.parqueaderos.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;

import java.util.List;

@ApplicationScoped
public class ParqueaderoServicio {

    //MÉTODO CREAR PARQUEADERO
    @Inject
    ParqueaderoRepositorio repositorio;
    @Transactional
    public void crearPark(Parqueadero parqueadero){
        //Cantidad de cupos disponibles debe ser inicialmente la misma cantidad de cupos totales
        if(parqueadero.getCapacidadTotal() > 0){
            parqueadero.setCuposDisponibles(parqueadero.getCapacidadTotal());
        }
        repositorio.crearPark(parqueadero);
    }


}
