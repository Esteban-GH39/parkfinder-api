package org.parkfinder.reservas.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** HU-11: el job le pide al servicio liberar las retenciones vencidas; la regla vive en el repositorio. */
class LiberacionReservasJobHu11Test {

    private ReservaRepositorio repositorio;
    private ReservaServicio servicio;
    private LiberacionReservasJob job;

    @BeforeEach
    void preparar() {
        repositorio = mock(ReservaRepositorio.class);
        servicio = new ReservaServicio();
        servicio.repositorio = repositorio;
        job = new LiberacionReservasJob();
        job.reservaServicio = servicio;
    }

    @Test
    void elServicioDevuelveCuantasLibero() {
        when(repositorio.liberarVencidas()).thenReturn(3);

        assertEquals(3, servicio.liberarVencidas());
    }

    @Test
    void cadaEjecucionDelJobLiberaLasVencidas() {
        when(repositorio.liberarVencidas()).thenReturn(0);

        job.liberarVencidas();
        job.liberarVencidas();

        verify(repositorio, org.mockito.Mockito.times(2)).liberarVencidas();
    }
}
