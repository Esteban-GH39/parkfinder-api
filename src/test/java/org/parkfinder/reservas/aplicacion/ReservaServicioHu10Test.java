package org.parkfinder.reservas.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkfinder.reservas.dominio.excepcion.ReservaEstadoInvalidoException;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** HU-10: el servicio pasa al repositorio el tiempo límite configurado; las reglas viven en el repositorio. */
class ReservaServicioHu10Test {

    private ReservaRepositorio repositorio;
    private ReservaServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(ReservaRepositorio.class);
        servicio = new ReservaServicio();
        servicio.repositorio = repositorio;
        servicio.cancelacionMinutosAntes = 90;
    }

    @Test
    void cancelarUsaElLimiteConfigurado() {
        Reserva cancelada = Reserva.builder().id(5L).clienteId(1L).estado(EstadoReserva.CANCELADA).build();
        when(repositorio.cancelarConfirmada(5L, 1L, Duration.ofMinutes(90))).thenReturn(cancelada);

        assertEquals(EstadoReserva.CANCELADA, servicio.cancelar(5L, 1L).estado);
        verify(repositorio).cancelarConfirmada(5L, 1L, Duration.ofMinutes(90));
    }

    @Test
    void elErrorDelRepositorioLlegaSinCambios() {
        when(repositorio.cancelarConfirmada(any(), any(), any()))
                .thenThrow(new ReservaEstadoInvalidoException("Ya pasó el tiempo límite para cancelar"));

        assertThrows(ReservaEstadoInvalidoException.class, () -> servicio.cancelar(5L, 1L));
    }
}
