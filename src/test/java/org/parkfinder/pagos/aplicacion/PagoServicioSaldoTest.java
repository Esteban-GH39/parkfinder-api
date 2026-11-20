package org.parkfinder.pagos.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkfinder.billetera.aplicacion.BilleteraServicio;
import org.parkfinder.billetera.dominio.excepcion.SaldoInsuficienteException;
import org.parkfinder.comprobante.aplicacion.ComprobanteServicio;
import org.parkfinder.comprobante.dominio.modelo.Comprobante;
import org.parkfinder.pagos.dominio.modelo.EstadoPago;
import org.parkfinder.pagos.dominio.modelo.MedioPago;
import org.parkfinder.pagos.dominio.modelo.Pago;
import org.parkfinder.pagos.dominio.pasarela.PasarelaPago;
import org.parkfinder.pagos.dominio.repositorio.PagoRepositorio;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pagar una reserva con el saldo de la billetera: no pasa por la pasarela y no cuenta como intento. */
class PagoServicioSaldoTest {

    private PagoRepositorio repositorio;
    private ReservaRepositorio reservaRepositorio;
    private PasarelaPago pasarela;
    private BilleteraServicio billetera;
    private PagoServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(PagoRepositorio.class);
        reservaRepositorio = mock(ReservaRepositorio.class);
        pasarela = mock(PasarelaPago.class);
        billetera = mock(BilleteraServicio.class);
        ComprobanteServicio comprobanteServicio = mock(ComprobanteServicio.class);
        servicio = new PagoServicio();
        servicio.repositorio = repositorio;
        servicio.reservaRepositorio = reservaRepositorio;
        servicio.comprobanteServicio = comprobanteServicio;
        servicio.pasarela = pasarela;
        servicio.billetera = billetera;
        servicio.costoReserva = 3000;
        servicio.maxIntentos = 3;

        when(reservaRepositorio.obtenerPorId(5L)).thenReturn(Reserva.builder()
                .id(5L).clienteId(1L).parqueaderoId(7L).cantidad(1).estado(EstadoReserva.RETENIDA).build());
        when(repositorio.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(comprobanteServicio.generar(any())).thenAnswer(i -> {
            Comprobante comprobante = i.getArgument(0);
            comprobante.idComprobante = 77L;
            return comprobante;
        });
    }

    @Test
    void pagarConSaldoDescuentaElCostoYConfirmaLaReserva() {
        Pago pago = servicio.pagar(5L, MedioPago.SALDO, Map.of());

        assertEquals(EstadoPago.APROBADO, pago.estado);
        assertEquals(3000.0, pago.monto);
        assertEquals(MedioPago.SALDO, pago.medioPago);
        assertEquals(77L, pago.comprobanteId);
        verify(billetera).cobrarReserva(eq(1L), eq(5L), eq(3000L), anyString());
        verify(reservaRepositorio).confirmar(5L);
        verify(pasarela, never()).procesar(any(), any(), anyDouble());
    }

    @Test
    void conSaldoInsuficienteNoSeGuardaPagoNiSeConfirmaNiSeCuentaIntento() {
        doThrow(new SaldoInsuficienteException("Saldo insuficiente"))
                .when(billetera).cobrarReserva(anyLong(), anyLong(), anyLong(), anyString());

        assertThrows(SaldoInsuficienteException.class, () -> servicio.pagar(5L, MedioPago.SALDO, Map.of()));

        verify(repositorio, never()).guardar(any());
        verify(reservaRepositorio, never()).confirmar(anyLong());
        verify(reservaRepositorio, never()).cancelar(anyLong());
    }
}
