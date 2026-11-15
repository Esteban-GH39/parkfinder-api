package org.parkfinder.estadias.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.parkfinder.estadias.aplicacion.EstadiaServicio.VehiculoIngreso;
import org.parkfinder.estadias.dominio.excepcion.EstadiaException;
import org.parkfinder.estadias.dominio.modelo.Estadia;
import org.parkfinder.estadias.dominio.modelo.Liquidacion;
import org.parkfinder.estadias.dominio.repositorio.EstadiaRepositorio;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Ingreso y salida de vehículos por placa, sin levantar Quarkus ni la base de datos. */
class EstadiaServicioTest {

    private EstadiaRepositorio repositorio;
    private ParqueaderoRepositorio parqueaderos;
    private ReservaRepositorio reservas;
    private EstadiaServicio servicio;

    private final LocalDateTime ahora = LocalDateTime.of(2026, 11, 15, 10, 45);

    @BeforeEach
    void preparar() {
        repositorio = mock(EstadiaRepositorio.class);
        parqueaderos = mock(ParqueaderoRepositorio.class);
        reservas = mock(ReservaRepositorio.class);
        servicio = new EstadiaServicio();
        servicio.repositorio = repositorio;
        servicio.parqueaderoRepositorio = parqueaderos;
        servicio.reservaRepositorio = reservas;
        servicio.reloj = () -> ahora;

        when(parqueaderos.buscarPorId(7L)).thenReturn(Optional.of(Parqueadero.builder()
                .id(7L).fk_parqueadero_admin(1).capacidadTotal(10).capacidadMotos(4)
                .tarifaHora(3000.0).tarifaDia(25000.0).tarifaNoche(15000.0)
                .tarifaHoraMoto(1200.0).tarifaDiaMoto(10000.0)
                .build()));
        when(repositorio.registrarIngreso(any(), anyBoolean())).thenAnswer(i -> i.getArgument(0));
        when(repositorio.buscarActivaPorPlaca(any())).thenReturn(Optional.empty());
    }

    private Estadia adentro(String placa, TipoVehiculo tipo, LocalDateTime ingreso) {
        return Estadia.builder().id(3L).parqueaderoId(7L).placa(placa).tipoVehiculo(tipo).ingresoEn(ingreso).build();
    }

    @Test
    void ingresoDirecto_normalizaLaPlacaGuardaLaHoraYDescuentaCupo() {
        Estadia estadia = servicio.ingresoDirecto(7L, 1, " abc-123 ", TipoVehiculo.CARRO);

        assertEquals("ABC123", estadia.placa);
        assertEquals(ahora, estadia.ingresoEn);
        assertNull(estadia.reservaId);
        verify(repositorio).registrarIngreso(any(), eq(true));
    }

    @Test
    void ingresoDirecto_placaInvalidaSeRechaza() {
        EstadiaException e = assertThrows(EstadiaException.class,
                () -> servicio.ingresoDirecto(7L, 1, "AB", TipoVehiculo.CARRO));
        assertEquals(400, e.getStatus());
        verify(repositorio, never()).registrarIngreso(any(), anyBoolean());
    }

    @Test
    void ingresoDirecto_deOtroAdministradorSeRechaza() {
        EstadiaException e = assertThrows(EstadiaException.class,
                () -> servicio.ingresoDirecto(7L, 2, "ABC123", TipoVehiculo.CARRO));
        assertEquals(403, e.getStatus());
    }

    @Test
    void ingresoDirecto_placaQueYaEstaAdentroSeRechaza() {
        when(repositorio.buscarActivaPorPlaca("ABC123"))
                .thenReturn(Optional.of(adentro("ABC123", TipoVehiculo.CARRO, ahora.minusHours(1))));

        EstadiaException e = assertThrows(EstadiaException.class,
                () -> servicio.ingresoDirecto(7L, 1, "ABC123", TipoVehiculo.CARRO));
        assertEquals(409, e.getStatus());
    }

    @Test
    void ingresoConReserva_validaElCodigoYNoDescuentaCupo() {
        when(reservas.obtenerPorId(5L)).thenReturn(Reserva.builder().id(5L).parqueaderoId(7L)
                .cantidad(2).cantidadMotos(1).estado(EstadoReserva.CONFIRMADA).build());

        List<Estadia> estadias = servicio.ingresoConReserva(5L, 7L, 1, List.of(
                new VehiculoIngreso("abc123", TipoVehiculo.CARRO),
                new VehiculoIngreso("xyz12d", TipoVehiculo.MOTO)));

        assertEquals(2, estadias.size());
        assertEquals(5L, estadias.get(0).reservaId);
        assertEquals("XYZ12D", estadias.get(1).placa);
        verify(reservas).validar(5L);
        verify(repositorio, never()).registrarIngreso(any(), eq(true));
    }

    @Test
    void ingresoConReserva_masPlacasQueLasReservadasSeRechaza() {
        when(reservas.obtenerPorId(5L)).thenReturn(Reserva.builder().id(5L).parqueaderoId(7L)
                .cantidad(1).cantidadMotos(0).build());

        EstadiaException e = assertThrows(EstadiaException.class, () -> servicio.ingresoConReserva(5L, 7L, 1,
                List.of(new VehiculoIngreso("ABC123", TipoVehiculo.CARRO),
                        new VehiculoIngreso("DEF456", TipoVehiculo.CARRO))));
        assertEquals(400, e.getStatus());
        verify(reservas, never()).validar(anyLong());
    }

    @Test
    void ingresoConReserva_deOtroParqueaderoSeRechaza() {
        when(reservas.obtenerPorId(5L)).thenReturn(Reserva.builder().id(5L).parqueaderoId(8L).cantidad(1).build());

        assertThrows(EstadiaException.class, () -> servicio.ingresoConReserva(5L, 7L, 1,
                List.of(new VehiculoIngreso("ABC123", TipoVehiculo.CARRO))));
        verify(reservas, never()).validar(anyLong());
    }

    @Test
    void consultarCobro_carroCobraElMinutoExactoSinLiberar() {
        when(repositorio.buscarActivaPorPlaca("ABC123"))
                .thenReturn(Optional.of(adentro("ABC123", TipoVehiculo.CARRO, ahora.minusMinutes(45))));

        Liquidacion l = servicio.consultarCobro(7L, 1, "abc123");

        assertEquals(2250.0, l.cobro().valorTotal());
        assertEquals(45, l.cobro().minutos());
        verify(repositorio, never()).registrarSalida(anyLong(), any(), anyLong(), anyDouble());
    }

    @Test
    void consultarCobro_motoUsaSusTarifasYLaNocheDelCarroSiNoTienePropia() {
        when(repositorio.buscarActivaPorPlaca("XYZ12D"))
                .thenReturn(Optional.of(adentro("XYZ12D", TipoVehiculo.MOTO, ahora.minusMinutes(30))));

        Liquidacion l = servicio.consultarCobro(7L, 1, "XYZ12D");

        assertEquals(1200.0, l.tarifas().hora());
        assertEquals(10000.0, l.tarifas().dia());
        assertEquals(15000.0, l.tarifas().noche());
        assertEquals(600.0, l.cobro().valorTotal());
    }

    @Test
    void registrarSalida_guardaElValorYLiberaElCupo() {
        when(repositorio.buscarActivaPorPlaca("ABC123"))
                .thenReturn(Optional.of(adentro("ABC123", TipoVehiculo.CARRO, ahora.minusMinutes(90))));
        when(repositorio.registrarSalida(eq(3L), eq(ahora), eq(90L), eq(4500.0)))
                .thenReturn(adentro("ABC123", TipoVehiculo.CARRO, ahora.minusMinutes(90)));

        Liquidacion l = servicio.registrarSalida(7L, 1, "ABC123");

        assertTrue(l.cerrada());
        assertEquals(4500.0, l.cobro().valorTotal());
        ArgumentCaptor<Double> valor = ArgumentCaptor.forClass(Double.class);
        verify(repositorio).registrarSalida(eq(3L), eq(ahora), eq(90L), valor.capture());
        assertEquals(4500.0, valor.getValue());
    }

    @Test
    void registrarSalida_placaQueNoEstaEnEsteParqueaderoDa404() {
        Estadia enOtro = adentro("ABC123", TipoVehiculo.CARRO, ahora.minusHours(1));
        enOtro.parqueaderoId = 8L;
        when(repositorio.buscarActivaPorPlaca("ABC123")).thenReturn(Optional.of(enOtro));

        EstadiaException e = assertThrows(EstadiaException.class, () -> servicio.registrarSalida(7L, 1, "ABC123"));
        assertEquals(404, e.getStatus());
    }
}
