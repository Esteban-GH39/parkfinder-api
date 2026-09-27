package org.parkfinder.parqueaderos.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.parkfinder.parqueaderos.dominio.modelo.HistorialCambio;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoActualizacion;
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

    //MÉTODO EDITAR PARQUEADERO (HU-19)
    public List<HistorialCambio> actualizarPark(Long id, ParqueaderoActualizacion datos){
        return repositorio.actualizarPark(id, datos);
    }

    //MÉTODO CONSULTAR HISTORIAL DE CAMBIOS (HU-19)
    public List<HistorialCambio> historial(Long id){
        return repositorio.historial(id);
    }

}
