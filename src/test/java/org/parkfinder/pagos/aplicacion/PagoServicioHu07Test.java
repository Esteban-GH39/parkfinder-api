package org.parkfinder.pagos.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.parkfinder.comprobante.aplicacion.ComprobanteServicio;
import org.parkfinder.comprobante.dominio.modelo.Comprobante;
import org.parkfinder.pagos.dominio.excepcion.PagoInvalidoException;
import org.parkfinder.pagos.dominio.excepcion.PagoNoPermitidoException;
import org.parkfinder.pagos.dominio.modelo.EstadoPago;
import org.parkfinder.pagos.dominio.modelo.MedioPago;
import org.parkfinder.pagos.dominio.modelo.Pago;
import org.parkfinder.pagos.dominio.repositorio.PagoRepositorio;
import org.parkfinder.pagos.infraestructura.PasarelaSimulada;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-07: reglas del pago con la pasarela simulada, sin levantar Quarkus ni la base de datos.
 * La pasarela es la real (simulada); el resto de puertos se simula.
 */
class PagoServicioHu07Test {

    private static final String VENCE = String.format("%02d/%02d",
            YearMonth.now().plusYears(2).getMonthValue(), YearMonth.now().plusYears(2).getYear() % 100);

    private PagoRepositorio repositorio;
    private ReservaRepositorio reservaRepositorio;
    private ComprobanteServicio comprobanteServicio;
    private PagoServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(PagoRepositorio.class);
        reservaRepositorio = mock(ReservaRepositorio.class);
        comprobanteServicio = mock(ComprobanteServicio.class);
        servicio = new PagoServicio();
        servicio.repositorio = repositorio;
        servicio.reservaRepositorio = reservaRepositorio;
        servicio.comprobanteServicio = comprobanteServicio;
        servicio.pasarela = new PasarelaSimulada();
        servicio.costoReserva = 3000;

        when(reservaRepositorio.obtenerPorId(5L)).thenReturn(reserva(EstadoReserva.RETENIDA));
        when(repositorio.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(comprobanteServicio.generar(any())).thenAnswer(invocacion -> {
            Comprobante comprobante = invocacion.getArgument(0);
            comprobante.idComprobante = 77L;
            return comprobante;
        });
    }

    private Reserva reserva(EstadoReserva estado) {
        return Reserva.builder().id(5L).clienteId(1L).parqueaderoId(7L).cantidad(1).estado(estado).build();
    }

    private Map<String, String> tarjeta(String numero) {
        return Map.of("numeroTarjeta", numero, "titular", "Laura Gomez", "vencimiento", VENCE, "cvv", "123");
    }

    private Map<String, String> tarjetaCon(String campo, String valor) {
        Map<String, String> datos = new HashMap<>(tarjeta("4242 4242 4242 4242"));
        datos.put(campo, valor);
        return datos;
    }

    @Test
    void cualquierTarjetaValidaSeApruebaYCobraElCostoFijo() {
        Pago pago = servicio.pagar(5L, MedioPago.TARJETA, tarjeta("4242 4242 4242 4242"));

        assertEquals(EstadoPago.APROBADO, pago.estado);
        assertEquals(3000.0, pago.monto);
        assertEquals("Tarjeta **** 4242", pago.detalle);
        assertNotNull(pago.referencia);
    }

    @Test
    void elCostoEsElMismoSinImportarCuantosCuposNiDeQueTipo() {
        when(reservaRepositorio.obtenerPorId(5L)).thenReturn(Reserva.builder().id(5L).clienteId(1L)
                .parqueaderoId(7L).cantidad(6).cantidadMotos(2).estado(EstadoReserva.RETENIDA).build());

        assertEquals(3000.0, servicio.pagar(5L, MedioPago.TARJETA, tarjeta("4242424242424242")).monto);
    }

    @Test
    void pagoAprobadoConfirmaLaReservaYGeneraSuCodigo() {
        Pago pago = servicio.pagar(5L, MedioPago.TARJETA, tarjeta("4242424242424242"));

        verify(reservaRepositorio).confirmar(5L);
        ArgumentCaptor<Comprobante> comprobante = ArgumentCaptor.forClass(Comprobante.class);
        verify(comprobanteServicio).generar(comprobante.capture());
        assertEquals(5L, comprobante.getValue().reservaId);
        assertEquals(1L, comprobante.getValue().fk_comprobante_cliente);
        assertEquals(7L, comprobante.getValue().fk_comprobante_parqueadero);
        assertEquals(3000.0, comprobante.getValue().valorPago);
        assertEquals("APROBADO", comprobante.getValue().estadoTransaccion);
        assertEquals(77L, pago.comprobanteId, "el código de reserva es el id del comprobante");
    }

    @Test
    void tarjetaTerminadaEnCerosSeRechazaPeroSeGuardaSinConfirmarNiGenerarCodigo() {
        Pago pago = servicio.pagar(5L, MedioPago.TARJETA, tarjeta("4000 0000 0000 0000"));

        assertEquals(EstadoPago.RECHAZADO, pago.estado);
        verify(repositorio).guardar(pago);
        verify(reservaRepositorio, never()).confirmar(any());
        verify(comprobanteServicio, never()).generar(any());
    }

    @Test
    void pseYNequiSeApruebanConSusDatos() {
        assertEquals(EstadoPago.APROBADO,
                servicio.pagar(5L, MedioPago.PSE, Map.of("banco", "Bancolombia", "documento", "1234567890")).estado);
        assertEquals(EstadoPago.APROBADO,
                servicio.pagar(5L, MedioPago.NEQUI, Map.of("celular", "3001234567")).estado);
    }

    @Test
    void laTarjetaDebeTenerDieciseisDigitos() {
        assertThrows(PagoInvalidoException.class, () -> servicio.pagar(5L, MedioPago.TARJETA, tarjeta("4242 4242 4242 424")));
        assertThrows(PagoInvalidoException.class, () -> servicio.pagar(5L, MedioPago.TARJETA, tarjeta("4242 4242 4242 42424")));
        assertThrows(PagoInvalidoException.class, () -> servicio.pagar(5L, MedioPago.TARJETA, tarjeta("4242 4242 4242 424a")));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void elVencimientoDebeTenerFormatoMMAAYNoEstarVencido() {
        assertThrows(PagoInvalidoException.class,
                () -> servicio.pagar(5L, MedioPago.TARJETA, tarjetaCon("vencimiento", "1/30")));
        assertThrows(PagoInvalidoException.class,
                () -> servicio.pagar(5L, MedioPago.TARJETA, tarjetaCon("vencimiento", "13/30")));
        assertThrows(PagoInvalidoException.class,
                () -> servicio.pagar(5L, MedioPago.TARJETA, tarjetaCon("vencimiento", "01/20")));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void elCvvDebeTenerTresOCuatroDigitos() {
        assertThrows(PagoInvalidoException.class, () -> servicio.pagar(5L, MedioPago.TARJETA, tarjetaCon("cvv", "12")));
        assertThrows(PagoInvalidoException.class, () -> servicio.pagar(5L, MedioPago.TARJETA, tarjetaCon("cvv", "12345")));
        assertEquals(EstadoPago.APROBADO, servicio.pagar(5L, MedioPago.TARJETA, tarjetaCon("cvv", "1234")).estado);
    }

    @Test
    void pseYNequiValidanDocumentoYCelular() {
        assertThrows(PagoInvalidoException.class,
                () -> servicio.pagar(5L, MedioPago.PSE, Map.of("banco", "Bancolombia", "documento", "123")));
        assertThrows(PagoInvalidoException.class,
                () -> servicio.pagar(5L, MedioPago.NEQUI, Map.of("celular", "2001234567")));
        assertThrows(PagoInvalidoException.class,
                () -> servicio.pagar(5L, MedioPago.NEQUI, Map.of("celular", "300123")));
    }

    @Test
    void faltandoUnDatoNoProcesaNada() {
        assertThrows(PagoInvalidoException.class,
                () -> servicio.pagar(5L, MedioPago.TARJETA, Map.of("numeroTarjeta", "4242424242424242")));
        assertThrows(PagoInvalidoException.class, () -> servicio.pagar(5L, MedioPago.NEQUI, null));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void noSePuedePagarUnaReservaQueYaNoEstaRetenida() {
        when(reservaRepositorio.obtenerPorId(5L)).thenReturn(reserva(EstadoReserva.EXPIRADA));

        assertThrows(PagoNoPermitidoException.class,
                () -> servicio.pagar(5L, MedioPago.TARJETA, tarjeta("4242424242424242")));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void noSePagaDosVecesLaMismaReserva() {
        when(repositorio.existeAprobado(5L)).thenReturn(true);

        assertThrows(PagoNoPermitidoException.class,
                () -> servicio.pagar(5L, MedioPago.TARJETA, tarjeta("4242424242424242")));
    }
}
