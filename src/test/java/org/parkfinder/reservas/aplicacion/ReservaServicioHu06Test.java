package org.parkfinder.reservas.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.parkfinder.parqueaderos.dominio.excepcion.ParqueaderoNoEncontradoException;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoConsulta;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;
import org.parkfinder.reservas.dominio.excepcion.ReservaInvalidaException;
import org.parkfinder.reservas.dominio.excepcion.SinCupoException;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-06: pruebas de las reglas de la reserva sin levantar Quarkus ni tocar la base de datos.
 * El repositorio se simula, así que solo se verifica lo que el servicio decide antes de retener.
 */
class ReservaServicioHu06Test {

    private ReservaRepositorio repositorio;
    private ParqueaderoRepositorio parqueaderoRepositorio;
    private ReservaServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(ReservaRepositorio.class);
        parqueaderoRepositorio = mock(ParqueaderoRepositorio.class);
        servicio = new ReservaServicio();
        servicio.repositorio = repositorio;
        servicio.parqueaderoRepositorio = parqueaderoRepositorio;
        servicio.retencionMinutos = 10;

        when(parqueaderoRepositorio.obtenerPorId(7L)).thenReturn(ParqueaderoConsulta.builder()
                .id(7L)
                .nombre("Parqueadero Centro")
                .horaInicio(LocalTime.of(7, 0))
                .horaFin(LocalTime.of(22, 0))
                .build());
        when(repositorio.retenerCupo(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private LocalDateTime llegadaManana(int hora) {
        return LocalDate.now().plusDays(1).atTime(hora, 0);
    }

    @Test
    void reservar_conCupoRetieneLaReservaPorElTiempoConfigurado() {
        Reserva reserva = servicio.reservar(1L, 7L, llegadaManana(10));

        assertEquals(EstadoReserva.RETENIDA, reserva.estado);
        assertEquals(1L, reserva.clienteId);
        assertEquals(7L, reserva.parqueaderoId);
        assertEquals(Duration.ofMinutes(10), Duration.between(reserva.creadaEn, reserva.expiraEn));
    }

    @Test
    void reservar_sinCupoPropagaElErrorYNoGeneraRetencion() {
        when(repositorio.retenerCupo(any())).thenThrow(new SinCupoException(7L));

        assertThrows(SinCupoException.class, () -> servicio.reservar(1L, 7L, llegadaManana(10)));
    }

    @Test
    void reservar_conLlegadaPasadaSeRechazaSinConsultarNiRetener() {
        LocalDateTime ayer = LocalDateTime.now().minusDays(1);

        assertThrows(ReservaInvalidaException.class, () -> servicio.reservar(1L, 7L, ayer));

        verify(repositorio, never()).retenerCupo(any());
    }

    @Test
    void reservar_fueraDelHorarioDelParqueaderoSeRechaza() {
        assertThrows(ReservaInvalidaException.class, () -> servicio.reservar(1L, 7L, llegadaManana(3)));

        verify(repositorio, never()).retenerCupo(any());
    }

    @Test
    void reservar_parqueaderoInexistentePropagaNoEncontrado() {
        when(parqueaderoRepositorio.obtenerPorId(99L)).thenThrow(new ParqueaderoNoEncontradoException(99L));

        assertThrows(ParqueaderoNoEncontradoException.class,
                () -> servicio.reservar(1L, 99L, llegadaManana(10)));

        verify(repositorio, never()).retenerCupo(any());
    }

    @Test
    void reservar_entregaAlRepositorioLaReservaConSuVencimiento() {
        servicio.reservar(1L, 7L, llegadaManana(10));

        ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
        verify(repositorio).retenerCupo(captor.capture());
        assertEquals(llegadaManana(10), captor.getValue().fechaHoraLlegada);
    }
}
