package org.parkfinder.parqueaderos.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;

import java.util.List;

@ApplicationScoped
public class ParqueaderoServicio {

    //MÉTODO CREAR PARQUEADERO
    @Inject
    ParqueaderoRepositorio repositorio;
    public void crearPark(Parqueadero parqueadero){
        repositorio.crearPark(parqueadero);
    }


}
