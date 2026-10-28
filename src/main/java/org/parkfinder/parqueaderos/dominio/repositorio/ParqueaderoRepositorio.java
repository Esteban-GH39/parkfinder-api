package org.parkfinder.parqueaderos.dominio.repositorio;

import org.parkfinder.parqueaderos.dominio.modelo.HistorialCambio;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoActualizacion;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoConsulta;

import java.util.List;
import java.util.Optional;


public interface ParqueaderoRepositorio {

    //METODO CREAR PARQUEADERO
    void crearPark(Parqueadero parqueadero);

    //METODO EDITAR PARQUEADERO (HU-19). Devuelve los cambios registrados.
    List<HistorialCambio> actualizarPark(Long id, ParqueaderoActualizacion datos);

    //METODO CONSULTAR HISTORIAL DE CAMBIOS (HU-19)
    List<HistorialCambio> historial(Long id);

    //METODO CONSULTAR UN PARQUEADERO POR ID (HU-19). Lanza ParqueaderoNoEncontradoException si no existe.
    ParqueaderoConsulta obtenerPorId(Long id);

    //METODO LISTAR PARQUEADEROS (HU-19). Sin filtros: la búsqueda es de HU-03.
    List<ParqueaderoConsulta> listar();


    //METODO PARA LISTAR PARQUEADEROS POR ID DE ADMIN (HU-21)
    List<Parqueadero>listarPorAdmin(Integer id);

    //METODO PARA ACTUALIZAR CUPOS DE PARQUEADERO
    Optional<Parqueadero> buscarPorId(Long id);
    void actualizarCupos(Long id, Integer cuposdisponibles);

    //METODO BUSCAR PARQUEADEROS POR ZONA (HU-03). Coincidencia parcial, sin distinguir mayúsculas.
    List<ParqueaderoConsulta> buscarPorZona(String zona);

    //METODO LISTAR ZONAS EXISTENTES (HU-03). Sin repetidas, ordenadas alfabéticamente.
    List<String> listarZonas();

}
