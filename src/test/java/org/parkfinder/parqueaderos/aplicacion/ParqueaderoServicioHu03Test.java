package org.parkfinder.parqueaderos.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoConsulta;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** HU-03: búsqueda por zona. Sin Quarkus ni base de datos. */
class ParqueaderoServicioHu03Test {

    private ParqueaderoRepositorio repositorio;
    private ParqueaderoServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(ParqueaderoRepositorio.class);
        servicio = new ParqueaderoServicio();
        servicio.repositorio = repositorio;
    }

    @Test
    void buscar_conZonaFiltraPorZona() {
        List<ParqueaderoConsulta> esperado = List.of(ParqueaderoConsulta.builder().zona("Chapinero").build());
        when(repositorio.buscarPorZona("Chapinero")).thenReturn(esperado);

        assertEquals(esperado, servicio.buscar("Chapinero"));
    }

    @Test
    void buscar_sinZonaODeEspaciosDevuelveTodos() {
        when(repositorio.listar()).thenReturn(List.of(ParqueaderoConsulta.builder().build()));

        assertEquals(1, servicio.buscar(null).size());
        assertEquals(1, servicio.buscar("   ").size());
        verify(repositorio, never()).buscarPorZona(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void buscar_sinCoincidenciasDevuelveListaVacia() {
        when(repositorio.buscarPorZona("Marte")).thenReturn(List.of());

        assertEquals(List.of(), servicio.buscar("Marte"));
    }

    @Test
    void listarZonas_delegaEnElRepositorio() {
        when(repositorio.listarZonas()).thenReturn(List.of("Centro", "Chapinero"));

        assertEquals(List.of("Centro", "Chapinero"), servicio.listarZonas());
    }
}
