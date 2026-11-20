package org.parkfinder.billetera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkfinder.billetera.aplicacion.BilleteraServicio;
import org.parkfinder.billetera.aplicacion.BilleteraServicio.Recarga;
import org.parkfinder.billetera.dominio.excepcion.BilleteraClienteNoEncontradoException;
import org.parkfinder.billetera.dominio.excepcion.RecargaInvalidaException;
import org.parkfinder.billetera.dominio.modelo.Movimiento;
import org.parkfinder.billetera.dominio.modelo.TipoMovimiento;
import org.parkfinder.billetera.dominio.repositorio.BilleteraRepositorio;
import org.parkfinder.clientes.dominio.modelo.Cliente;
import org.parkfinder.clientes.dominio.repositorio.ClienteRepositorio;
import org.parkfinder.pagos.dominio.excepcion.PagoInvalidoException;
import org.parkfinder.pagos.dominio.modelo.EstadoPago;
import org.parkfinder.pagos.dominio.modelo.MedioPago;
import org.parkfinder.pagos.infraestructura.PasarelaSimulada;

import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas de la billetera sin levantar Quarkus ni la base de datos. La pasarela es la simulada. */
class BilleteraServicioTest {

    private static final String VENCE = String.format("%02d/%02d",
            YearMonth.now().plusYears(2).getMonthValue(), YearMonth.now().plusYears(2).getYear() % 100);

    private BilleteraRepositorio repositorio;
    private ClienteRepositorio clienteRepositorio;
    private BilleteraServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(BilleteraRepositorio.class);
        clienteRepositorio = mock(ClienteRepositorio.class);
        servicio = new BilleteraServicio();
        servicio.repositorio = repositorio;
        servicio.clienteRepositorio = clienteRepositorio;
        servicio.pasarela = new PasarelaSimulada();
        servicio.recargaMinima = 5000;
        servicio.recargaMaxima = 500000;

        when(clienteRepositorio.buscarPorId(1L)).thenReturn(Optional.of(Cliente.builder().id(1L).build()));
        when(repositorio.acreditar(anyLong(), any(), anyLong(), any(), anyString(), anyString()))
                .thenAnswer(i -> Movimiento.builder()
                        .clienteId(i.getArgument(0)).tipo(i.getArgument(1)).monto(i.getArgument(2))
                        .saldoPosterior(30000L).build());
    }

    private Map<String, String> tarjeta(String numero) {
        return Map.of("numeroTarjeta", numero, "titular", "Laura Gomez", "vencimiento", VENCE, "cvv", "123");
    }

    @Test
    void recargaAprobadaAcreditaElSaldoYDevuelveElNuevo() {
        var recarga = servicio.recargar(1L, 20000, MedioPago.TARJETA, tarjeta("4242 4242 4242 4242"));

        assertEquals(EstadoPago.APROBADO, recarga.estado());
        assertEquals(30000L, recarga.saldo());
        assertNotNull(recarga.movimiento());
        verify(repositorio).acreditar(eq(1L), eq(TipoMovimiento.RECARGA), eq(20000L), isNull(),
                anyString(), anyString());
    }

    @Test
    void recargaRechazadaPorLaPasarelaNoCambiaElSaldo() {
        when(repositorio.saldoDe(1L)).thenReturn(8000L);

        var recarga = servicio.recargar(1L, 20000, MedioPago.TARJETA, tarjeta("4242 4242 4242 0000"));

        assertEquals(EstadoPago.RECHAZADO, recarga.estado());
        assertEquals(8000L, recarga.saldo());
        assertNull(recarga.movimiento());
        verify(repositorio, never()).acreditar(anyLong(), any(), anyLong(), any(), anyString(), anyString());
    }

    @Test
    void montoPorDebajoDelMinimoOPorEncimaDelMaximoSeRechaza() {
        assertThrows(RecargaInvalidaException.class,
                () -> servicio.recargar(1L, 4999, MedioPago.NEQUI, Map.of("celular", "3001234567")));
        assertThrows(RecargaInvalidaException.class,
                () -> servicio.recargar(1L, 500001, MedioPago.NEQUI, Map.of("celular", "3001234567")));
        verify(repositorio, never()).acreditar(anyLong(), any(), anyLong(), any(), anyString(), anyString());
    }

    @Test
    void noSePuedeRecargarElSaldoConElMismoSaldo() {
        assertThrows(RecargaInvalidaException.class,
                () -> servicio.recargar(1L, 20000, MedioPago.SALDO, Map.of()));
    }

    @Test
    void datosDelMedioInvalidosSeRechazanAntesDeTocarLaPasarela() {
        assertThrows(PagoInvalidoException.class,
                () -> servicio.recargar(1L, 20000, MedioPago.NEQUI, Map.of("celular", "123")));
        verify(repositorio, never()).acreditar(anyLong(), any(), anyLong(), any(), anyString(), anyString());
    }

    @Test
    void clienteInexistenteDaNoEncontrado() {
        when(clienteRepositorio.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(BilleteraClienteNoEncontradoException.class, () -> servicio.saldo(99L));
        assertThrows(BilleteraClienteNoEncontradoException.class,
                () -> servicio.recargar(99L, 20000, MedioPago.NEQUI, Map.of("celular", "3001234567")));
    }

    @Test
    void elLimiteDeMovimientosSeAcotaEntre1Y100() {
        servicio.movimientos(1L, 0);
        servicio.movimientos(1L, 5000);

        verify(repositorio).movimientos(1L, 1);
        verify(repositorio).movimientos(1L, 100);
    }

    @Test
    void elReembolsoSeHaceUnaSolaVezPorReserva() {
        when(repositorio.existeMovimiento(5L, TipoMovimiento.REEMBOLSO)).thenReturn(false, true);

        servicio.reembolsarReserva(1L, 5L, 3000);
        servicio.reembolsarReserva(1L, 5L, 3000);

        verify(repositorio, org.mockito.Mockito.times(1))
                .acreditar(eq(1L), eq(TipoMovimiento.REEMBOLSO), eq(3000L), eq(5L), anyString(), anyString());
    }
}
