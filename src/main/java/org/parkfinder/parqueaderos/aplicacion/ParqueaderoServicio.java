package org.parkfinder.parqueaderos.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.parkfinder.parqueaderos.dominio.modelo.HistorialCambio;

import jakarta.transaction.Transactional;
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
    @Transactional
    public void crearPark(Parqueadero parqueadero){
        //Cantidad de cupos disponibles debe ser inicialmente la misma cantidad de cupos totales
        if(parqueadero.getCapacidadTotal() > 0){
            parqueadero.setCuposDisponibles(parqueadero.getCapacidadTotal());
        }
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

    //METODO PARA LISTAR PARQUEADERO POR ADMIN (HU-21)
    public List<Parqueadero> listarPorAdmin(Integer id){
        return repositorio.listarPorAdmin(id);
    }

    //MEOTODO PARA ACTUALIZAR CUPOS DISPONIBLES DEL PARQUEADERO
    @Transactional
    public void actualizarCuposManualmente(Long parqueaderoId, int id, Integer cuposDisponibles){
        Parqueadero parqueadero = repositorio.buscarPorId(parqueaderoId)
                .orElseThrow(() -> new IllegalArgumentException("El parqueadero no existe"));

        // Validar pertenencia (Solo el administrador dueño puede modificar sus cupos)
        if (parqueadero.fk_parqueadero_admin != id) {
            throw new SecurityException("No tienes permisos para modificar este parqueadero");
        }

        // Validar capacidad máxima
        if (cuposDisponibles > parqueadero.capacidadTotal) {
            throw new IllegalArgumentException("La cantida de cupos ingresada (" + cuposDisponibles +
                    ") no pueden superar la capacidad total (" + parqueadero.capacidadTotal + ")");
        }

        //  Actualizar en BD
        repositorio.actualizarCupos(parqueaderoId, cuposDisponibles);

    }

}
