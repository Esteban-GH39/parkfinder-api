package org.parkfinder.reservas.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkfinder.billetera.aplicacion.BilleteraServicio;
import org.parkfinder.reservas.dominio.excepcion.ReservaEstadoInvalidoException;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Al cancelar una reserva confirmada, su costo vuelve a la billetera del cliente (si la política está activa). */
class ReservaServicioReembolsoTest {

    private ReservaRepositorio repositorio;
    private BilleteraServicio billetera;
    private ReservaServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(ReservaRepositorio.class);
        billetera = mock(BilleteraServicio.class);
        servicio = new ReservaServicio();
        servicio.repositorio = repositorio;
        servicio.billetera = billetera;
        servicio.cancelacionMinutosAntes = 60;
        servicio.costoReserva = 3000;
        servicio.reembolsarAlCancelar = true;
    }

    private void reservaCancelable() {
        when(repositorio.cancelarConfirmada(5L, 1L, Duration.ofMinutes(60)))
                .thenReturn(Reserva.builder().id(5L).clienteId(1L).estado(EstadoReserva.CANCELADA).build());
    }

    @Test
    void cancelarReembolsaElCostoDeLaReserva() {
        reservaCancelable();

        servicio.cancelar(5L, 1L);

        verify(billetera).reembolsarReserva(1L, 5L, 3000L);
    }

    @Test
    void conLaPoliticaDesactivadaNoSeReembolsa() {
        servicio.reembolsarAlCancelar = false;
        reservaCancelable();

        servicio.cancelar(5L, 1L);

        verify(billetera, never()).reembolsarReserva(anyLong(), anyLong(), anyLong());
    }

    @Test
    void siNoSePuedeCancelarNoSeReembolsa() {
        when(repositorio.cancelarConfirmada(any(), any(), any()))
                .thenThrow(new ReservaEstadoInvalidoException("Ya pasó el tiempo límite para cancelar"));

        assertThrows(ReservaEstadoInvalidoException.class, () -> servicio.cancelar(5L, 1L));

        verify(billetera, never()).reembolsarReserva(anyLong(), anyLong(), anyLong());
    }
}
