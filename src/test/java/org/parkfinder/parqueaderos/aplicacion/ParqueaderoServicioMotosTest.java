package org.parkfinder.parqueaderos.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cupos de carros y de motos del parqueadero, sin levantar Quarkus ni la base de datos.
 * capacidadTotal y cuposDisponibles son el total; capacidadMotos y cuposMotos, la parte de motos.
 */
class ParqueaderoServicioMotosTest {

    private ParqueaderoRepositorio repositorio;
    private ParqueaderoServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(ParqueaderoRepositorio.class);
        servicio = new ParqueaderoServicio();
        servicio.repositorio = repositorio;
    }

    private Parqueadero parqueadero(int total, Integer motos) {
        return Parqueadero.builder().nombre("P").capacidadTotal(total).capacidadMotos(motos).build();
    }

    @Test
    void crear_sinMotosDejaTodoComoCarros() {
        servicio.crearPark(parqueadero(10, null));

        ArgumentCaptor<Parqueadero> guardado = ArgumentCaptor.forClass(Parqueadero.class);
        verify(repositorio).crearPark(guardado.capture());
        assertEquals(0, guardado.getValue().capacidadMotos);
        assertEquals(0, guardado.getValue().cuposMotos);
        assertEquals(10, guardado.getValue().cuposDisponibles);
    }

    @Test
    void crear_conMotosEmpiezaConTodosLosCuposLibres() {
        servicio.crearPark(parqueadero(10, 4));

        ArgumentCaptor<Parqueadero> guardado = ArgumentCaptor.forClass(Parqueadero.class);
        verify(repositorio).crearPark(guardado.capture());
        assertEquals(4, guardado.getValue().cuposMotos);
        assertEquals(10, guardado.getValue().cuposDisponibles);
    }

    @Test
    void crear_conMasMotosQueCapacidadSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> servicio.crearPark(parqueadero(10, 11)));
        verify(repositorio, never()).crearPark(any());
    }

    private void parqueaderoGuardado(int total, int motos) {
        when(repositorio.buscarPorId(7L)).thenReturn(Optional.of(Parqueadero.builder()
                .fk_parqueadero_admin(1).capacidadTotal(total).capacidadMotos(motos).build()));
    }

    @Test
    void cupos_guardaTotalYMotosLibres() {
        parqueaderoGuardado(10, 4);

        servicio.actualizarCuposManualmente(7L, 1, 8, 3);

        verify(repositorio).actualizarCupos(7L, 8, 3);
    }

    @Test
    void cupos_motosLibresNoPuedenSuperarLasDeMotos() {
        parqueaderoGuardado(10, 4);

        assertThrows(IllegalArgumentException.class, () -> servicio.actualizarCuposManualmente(7L, 1, 8, 5));
        verify(repositorio, never()).actualizarCupos(any(), any(), any());
    }

    @Test
    void cupos_carrosLibresNoPuedenSuperarLosDeCarros() {
        // 6 cupos de carros en total: 8 libres con solo 1 de moto serían 7 de carros
        parqueaderoGuardado(10, 4);

        assertThrows(IllegalArgumentException.class, () -> servicio.actualizarCuposManualmente(7L, 1, 8, 1));
    }

    @Test
    void cupos_sinIndicarMotosUsaLaActualizacionDeSiempre() {
        parqueaderoGuardado(10, 4);

        servicio.actualizarCuposManualmente(7L, 1, 5);

        verify(repositorio).actualizarCupos(7L, 5);
        verify(repositorio, never()).actualizarCupos(any(), any(), any());
    }
}
