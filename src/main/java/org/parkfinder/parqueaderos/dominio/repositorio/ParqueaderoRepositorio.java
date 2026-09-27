package org.parkfinder.parqueaderos.dominio.repositorio;

import org.parkfinder.parqueaderos.dominio.modelo.HistorialCambio;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoActualizacion;

import java.util.List;


public interface ParqueaderoRepositorio {

    //METODO CREAR PARQUEADERO
    void crearPark(Parqueadero parqueadero);

    //METODO EDITAR PARQUEADERO (HU-19). Devuelve los cambios registrados.
    List<HistorialCambio> actualizarPark(Long id, ParqueaderoActualizacion datos);

    //METODO CONSULTAR HISTORIAL DE CAMBIOS (HU-19)
    List<HistorialCambio> historial(Long id);

}
