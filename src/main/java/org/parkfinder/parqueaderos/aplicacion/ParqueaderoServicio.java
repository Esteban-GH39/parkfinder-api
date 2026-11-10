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
        int motos = parqueadero.capacidadMotos == null ? 0 : parqueadero.capacidadMotos;
        if (motos > parqueadero.getCapacidadTotal()) {
            throw new IllegalArgumentException("Los cupos de motos (" + motos +
                    ") no pueden superar la capacidad total (" + parqueadero.getCapacidadTotal() + ")");
        }
        parqueadero.capacidadMotos = motos;
        parqueadero.cuposMotos = motos;
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
        actualizarCuposManualmente(parqueaderoId, id, cuposDisponibles, null);
    }

    //MÉTODO PARA ACTUALIZAR CUPOS LIBRES DE CARROS Y MOTOS: cuposDisponibles es el total y cuposMotos la parte de motos
    @Transactional
    public void actualizarCuposManualmente(Long parqueaderoId, int id, Integer cuposDisponibles, Integer cuposMotos){
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

        if (cuposMotos == null) {
            //  Actualizar en BD
            repositorio.actualizarCupos(parqueaderoId, cuposDisponibles);
            return;
        }
        int capacidadMotos = parqueadero.capacidadMotos == null ? 0 : parqueadero.capacidadMotos;
        if (cuposMotos > capacidadMotos) {
            throw new IllegalArgumentException("Los cupos libres de motos (" + cuposMotos +
                    ") no pueden superar los cupos de motos del parqueadero (" + capacidadMotos + ")");
        }
        int carros = cuposDisponibles - cuposMotos;
        int capacidadCarros = parqueadero.capacidadTotal - capacidadMotos;
        if (carros > capacidadCarros) {
            throw new IllegalArgumentException("Los cupos libres de carros (" + carros +
                    ") no pueden superar los cupos de carros del parqueadero (" + capacidadCarros + ")");
        }
        repositorio.actualizarCupos(parqueaderoId, cuposDisponibles, cuposMotos);

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
