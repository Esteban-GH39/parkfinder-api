package org.parkfinder.parqueaderos.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkfinder.parqueaderos.dominio.excepcion.ParqueaderoNoEncontradoException;
import org.parkfinder.parqueaderos.dominio.modelo.HistorialCambio;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoActualizacion;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoConsulta;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** HU-19: el servicio delega en el repositorio; se prueba sin Quarkus ni base de datos. */
class ParqueaderoServicioHu19Test {

    private ParqueaderoRepositorio repositorio;
    private ParqueaderoServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(ParqueaderoRepositorio.class);
        servicio = new ParqueaderoServicio();
        servicio.repositorio = repositorio;
    }

    @Test
    void actualizar_devuelveLosCambiosQueRegistroElRepositorio() {
        ParqueaderoActualizacion datos = ParqueaderoActualizacion.builder().zona("Usaquen").build();
        List<HistorialCambio> cambios = List.of(HistorialCambio.builder().campo("zona").build());
        when(repositorio.actualizarPark(7L, datos)).thenReturn(cambios);

        assertSame(cambios, servicio.actualizarPark(7L, datos));
    }

    @Test
    void actualizar_propagaNoEncontrado() {
        ParqueaderoActualizacion datos = ParqueaderoActualizacion.builder().build();
        when(repositorio.actualizarPark(99L, datos)).thenThrow(new ParqueaderoNoEncontradoException(99L));

        assertThrows(ParqueaderoNoEncontradoException.class, () -> servicio.actualizarPark(99L, datos));
    }

    @Test
    void obtenerPorId_devuelveElParqueaderoDelRepositorio() {
        ParqueaderoConsulta esperado = ParqueaderoConsulta.builder().id(7L).nombre("Centro").build();
        when(repositorio.obtenerPorId(7L)).thenReturn(esperado);

        assertSame(esperado, servicio.obtenerPorId(7L));
    }

    @Test
    void obtenerPorId_propagaNoEncontrado() {
        when(repositorio.obtenerPorId(99L)).thenThrow(new ParqueaderoNoEncontradoException(99L));

        assertThrows(ParqueaderoNoEncontradoException.class, () -> servicio.obtenerPorId(99L));
    }

    @Test
    void listar_devuelveTodosLosDelRepositorio() {
        List<ParqueaderoConsulta> todos = List.of(
                ParqueaderoConsulta.builder().id(1L).build(),
                ParqueaderoConsulta.builder().id(2L).build());
        when(repositorio.listar()).thenReturn(todos);

        assertEquals(2, servicio.listar().size());
    }

    @Test
    void historial_devuelveLosRenglonesDelRepositorio() {
        List<HistorialCambio> cambios = List.of(HistorialCambio.builder().campo("tarifaHora").build());
        when(repositorio.historial(7L)).thenReturn(cambios);

        assertSame(cambios, servicio.historial(7L));
    }
}
