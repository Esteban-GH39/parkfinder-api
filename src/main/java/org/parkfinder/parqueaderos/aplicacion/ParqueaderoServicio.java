package org.parkfinder.parqueaderos.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.parkfinder.parqueaderos.dominio.modelo.HistorialCambio;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoActualizacion;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoConsulta;
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

    //MÉTODO CONSULTAR UN PARQUEADERO (HU-19)
    public ParqueaderoConsulta obtenerPorId(Long id){
        return repositorio.obtenerPorId(id);
    }

    //MÉTODO LISTAR PARQUEADEROS (HU-19)
    public List<ParqueaderoConsulta> listar(){
        return repositorio.listar();
    }

    //MÉTODO BUSCAR POR ZONA (HU-03). Sin zona devuelve todos.
    public List<ParqueaderoConsulta> buscar(String zona){
        if (zona == null || zona.isBlank()) {
            return repositorio.listar();
        }
        return repositorio.buscarPorZona(zona);
    }

    //MÉTODO LISTAR ZONAS EXISTENTES (HU-03)
    public List<String> listarZonas(){
        return repositorio.listarZonas();
    }

}
