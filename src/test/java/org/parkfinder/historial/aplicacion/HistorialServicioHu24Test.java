package org.parkfinder.historial.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkfinder.historial.dominio.excepcion.HistorialException;
import org.parkfinder.historial.dominio.modelo.CobroEstadia;
import org.parkfinder.historial.dominio.modelo.Historial;
import org.parkfinder.historial.dominio.modelo.Periodo;
import org.parkfinder.historial.dominio.modelo.ReservaHistorial;
import org.parkfinder.historial.dominio.repositorio.HistorialRepositorio;
import org.parkfinder.parqueaderos.dominio.excepcion.ParqueaderoNoEncontradoException;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** HU-24: historial de reservas e ingresos, sin levantar Quarkus ni la base de datos. */
class HistorialServicioHu24Test {

    private HistorialRepositorio repositorio;
    private ParqueaderoRepositorio parqueaderos;
    private HistorialServicio servicio;

    // Domingo 15 de noviembre de 2026
    private final LocalDateTime ahora = LocalDateTime.of(2026, 11, 15, 12, 0);

    @BeforeEach
    void preparar() {
        repositorio = mock(HistorialRepositorio.class);
        parqueaderos = mock(ParqueaderoRepositorio.class);
        servicio = new HistorialServicio();
        servicio.repositorio = repositorio;
        servicio.parqueaderoRepositorio = parqueaderos;
        servicio.reloj = () -> ahora;

        when(parqueaderos.listarPorAdmin(1)).thenReturn(List.of(
                Parqueadero.builder().id(7L).fk_parqueadero_admin(1).nombre("Centro").build(),
                Parqueadero.builder().id(8L).fk_parqueadero_admin(1).nombre("Norte").build()));
        when(parqueaderos.buscarPorId(7L)).thenReturn(Optional.of(
                Parqueadero.builder().id(7L).fk_parqueadero_admin(1).nombre("Centro").build()));
        when(repositorio.listarReservas(any(), any(), any())).thenReturn(List.of());
        when(repositorio.listarCobros(any(), any(), any())).thenReturn(List.of());
    }

    private ReservaHistorial reserva(long id, EstadoReserva estado, LocalDateTime creada, double pagado) {
        return new ReservaHistorial(id, 7L, "Centro", "Laura", "laura@correo.com", 1, 0, null, estado,
                creada, creada.plusMinutes(10), creada.plusHours(2), null, pagado);
    }

    private CobroEstadia cobro(long id, LocalDateTime salida, double valor) {
        return new CobroEstadia(id, 7L, "Centro", null, "ABC123", TipoVehiculo.CARRO,
                salida.minusHours(1), salida, 60L, valor);
    }

    @Test
    void periodo_semanaVaDeLunesADomingoYMesDel1AlUltimoDia() {
        LocalDate miercoles = LocalDate.of(2026, 11, 11);
        assertEquals(new Periodo.Rango(LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 15)),
                Periodo.SEMANA.rango(miercoles));
        assertEquals(new Periodo.Rango(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)),
                Periodo.MES.rango(miercoles));
        assertEquals(new Periodo.Rango(miercoles, miercoles), Periodo.DIA.rango(miercoles));
        assertEquals(Periodo.DIA, Periodo.de("día"));
    }

    @Test
    void consultar_sumaPagosDeReservasYCobrosDeVehiculos() {
        when(repositorio.listarReservas(any(), any(), any())).thenReturn(List.of(
                reserva(1, EstadoReserva.CONFIRMADA, LocalDateTime.of(2026, 11, 10, 9, 0), 3000),
                reserva(2, EstadoReserva.CONFIRMADA, LocalDateTime.of(2026, 11, 12, 9, 0), 3000),
                reserva(3, EstadoReserva.EXPIRADA, LocalDateTime.of(2026, 11, 12, 10, 0), 0)));
        when(repositorio.listarCobros(any(), any(), any())).thenReturn(List.of(
                cobro(5, LocalDateTime.of(2026, 11, 12, 18, 0), 4500)));

        Historial h = servicio.consultar(1, null, "SEMANA", "2026-11-11");

        assertEquals(LocalDate.of(2026, 11, 9), h.desde());
        assertEquals(LocalDate.of(2026, 11, 15), h.hasta());
        assertEquals(6000, h.ingresosReservas());
        assertEquals(4500, h.ingresosEstadias());
        assertEquals(10500, h.ingresosTotales());
        assertEquals(3, h.totalReservas());
        assertEquals(2, h.reservasPagadas());
        assertEquals(1, h.vehiculosCobrados());
        // Un registro por día de la semana, incluso los días sin movimiento
        assertEquals(7, h.desglose().size());
        assertEquals(3000, h.desglose().get(1).total());
        assertEquals(7500, h.desglose().get(3).total());
        assertEquals(0, h.desglose().get(0).total());
    }

    @Test
    void consultar_buscaEntreElInicioDelPrimerDiaYElInicioDelDiaSiguienteAlUltimo() {
        servicio.consultar(1, null, "MES", "2026-11-20");

        verify(repositorio).listarReservas(eq(Map.of(7L, "Centro", 8L, "Norte")),
                eq(LocalDateTime.of(2026, 11, 1, 0, 0)), eq(LocalDateTime.of(2026, 12, 1, 0, 0)));
    }

    @Test
    void consultar_sinPeriodoNiFechaUsaElDiaDeHoy() {
        Historial h = servicio.consultar(1, null, null, null);

        assertEquals(Periodo.DIA, h.periodo());
        assertEquals(ahora.toLocalDate(), h.desde());
        assertEquals(ahora.toLocalDate(), h.hasta());
    }

    @Test
    void consultar_retencionVencidaSeMuestraExpirada() {
        when(repositorio.listarReservas(any(), any(), any())).thenReturn(List.of(
                reserva(1, EstadoReserva.RETENIDA, ahora.minusHours(1), 0)));

        Historial h = servicio.consultar(1, null, "DIA", null);

        assertEquals(EstadoReserva.EXPIRADA, h.reservas().get(0).estado());
    }

    @Test
    void consultar_unSoloParqueaderoDelDueno() {
        servicio.consultar(1, 7L, "DIA", null);

        verify(repositorio).listarReservas(eq(Map.of(7L, "Centro")), any(), any());
    }

    @Test
    void consultar_parqueaderoDeOtroDueno_lanza403() {
        HistorialException e = assertThrows(HistorialException.class,
                () -> servicio.consultar(2, 7L, "DIA", null));
        assertEquals(403, e.getStatus());
    }

    @Test
    void consultar_parqueaderoInexistente_lanzaNoEncontrado() {
        when(parqueaderos.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThrows(ParqueaderoNoEncontradoException.class, () -> servicio.consultar(1, 99L, "DIA", null));
    }

    @Test
    void consultar_periodoOFechaInvalidos_lanza400() {
        assertEquals(400, assertThrows(HistorialException.class,
                () -> servicio.consultar(1, null, "AÑO", null)).getStatus());
        assertEquals(400, assertThrows(HistorialException.class,
                () -> servicio.consultar(1, null, "DIA", "15/11/2026")).getStatus());
    }

    @Test
    void consultar_duenoSinParqueaderos_devuelveCerosSinConsultarLaBase() {
        when(parqueaderos.listarPorAdmin(3)).thenReturn(List.of());

        Historial h = servicio.consultar(3, null, "MES", null);

        assertEquals(0, h.ingresosTotales());
        assertTrue(h.reservas().isEmpty());
        verify(repositorio, never()).listarReservas(any(), any(), any());
    }
}
