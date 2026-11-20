package org.parkfinder.pagos.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkfinder.comprobante.aplicacion.ComprobanteServicio;
import org.parkfinder.pagos.dominio.modelo.EstadoPago;
import org.parkfinder.pagos.dominio.modelo.MedioPago;
import org.parkfinder.pagos.dominio.modelo.Pago;
import org.parkfinder.pagos.dominio.repositorio.PagoRepositorio;
import org.parkfinder.pagos.infraestructura.PasarelaSimulada;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import java.time.YearMonth;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** HU-08: la reserva se confirma solo con pago aprobado y el cupo se libera al agotar los intentos. */
class PagoServicioHu08Test {

    private static final String VENCE = String.format("%02d/%02d",
            YearMonth.now().plusYears(2).getMonthValue(), YearMonth.now().plusYears(2).getYear() % 100);

    private PagoRepositorio repositorio;
    private ReservaRepositorio reservaRepositorio;
    private PagoServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(PagoRepositorio.class);
        reservaRepositorio = mock(ReservaRepositorio.class);
        servicio = new PagoServicio();
        servicio.repositorio = repositorio;
        servicio.reservaRepositorio = reservaRepositorio;
        ComprobanteServicio comprobanteServicio = mock(ComprobanteServicio.class);
        when(comprobanteServicio.generar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        servicio.comprobanteServicio = comprobanteServicio;
        servicio.pasarela = new PasarelaSimulada();
        servicio.costoReserva = 3000;
        servicio.maxIntentos = 3;

        when(reservaRepositorio.obtenerPorId(5L)).thenReturn(
                Reserva.builder().id(5L).clienteId(1L).parqueaderoId(7L).cantidad(1)
                        .estado(EstadoReserva.RETENIDA).build());
        when(repositorio.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private Map<String, String> tarjeta(String numero) {
        return Map.of("numeroTarjeta", numero, "titular", "Laura Gomez", "vencimiento", VENCE, "cvv", "123");
    }

    @Test
    void unRechazoConIntentosPendientesNoCancelaYAvisaCuantosQuedan() {
        when(repositorio.contarRechazados(5L)).thenReturn(1L);

        Pago pago = servicio.pagar(5L, MedioPago.TARJETA, tarjeta("4000 0000 0000 0000"));

        assertEquals(EstadoPago.RECHAZADO, pago.estado);
        assertEquals(2, pago.intentosRestantes);
        assertFalse(pago.reservaCancelada);
        verify(reservaRepositorio, never()).cancelar(any());
        verify(reservaRepositorio, never()).confirmar(any());
    }

    @Test
    void elUltimoRechazoCancelaLaReservaYLiberaElCupo() {
        when(repositorio.contarRechazados(5L)).thenReturn(3L);

        Pago pago = servicio.pagar(5L, MedioPago.TARJETA, tarjeta("4000 0000 0000 0000"));

        assertEquals(0, pago.intentosRestantes);
        assertTrue(pago.reservaCancelada);
        verify(reservaRepositorio).cancelar(5L);
        verify(reservaRepositorio, never()).confirmar(any());
    }

    @Test
    void unPagoAprobadoNuncaCancelaLaReserva() {
        Pago pago = servicio.pagar(5L, MedioPago.TARJETA, tarjeta("4242 4242 4242 4242"));

        assertEquals(EstadoPago.APROBADO, pago.estado);
        assertFalse(pago.reservaCancelada);
        verify(reservaRepositorio).confirmar(5L);
        verify(reservaRepositorio, never()).cancelar(any());
    }
}
