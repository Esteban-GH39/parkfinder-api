package org.parkfinder.billetera.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.parkfinder.billetera.dominio.excepcion.BilleteraClienteNoEncontradoException;
import org.parkfinder.billetera.dominio.excepcion.RecargaInvalidaException;
import org.parkfinder.billetera.dominio.modelo.Movimiento;
import org.parkfinder.billetera.dominio.modelo.TipoMovimiento;
import org.parkfinder.billetera.dominio.repositorio.BilleteraRepositorio;
import org.parkfinder.clientes.dominio.repositorio.ClienteRepositorio;
import org.parkfinder.pagos.aplicacion.ValidadorMedioPago;
import org.parkfinder.pagos.dominio.modelo.EstadoPago;
import org.parkfinder.pagos.dominio.modelo.MedioPago;
import org.parkfinder.pagos.dominio.pasarela.PasarelaPago;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Billetera del cliente: recarga con la pasarela simulada, pago de reservas con saldo y reembolso al cancelar.
 * Todos los valores son pesos enteros.
 */
@ApplicationScoped
public class BilleteraServicio {

    /** Resultado de una recarga. Si la pasarela la rechaza no hay movimiento y el saldo no cambia. */
    public record Recarga(EstadoPago estado, String mensaje, String referencia, long saldo, Movimiento movimiento) {
    }

    @Inject
    BilleteraRepositorio repositorio;

    @Inject
    ClienteRepositorio clienteRepositorio;

    @Inject
    PasarelaPago pasarela;

    @ConfigProperty(name = "parkfinder.billetera.recarga-minima", defaultValue = "5000")
    long recargaMinima;

    @ConfigProperty(name = "parkfinder.billetera.recarga-maxima", defaultValue = "500000")
    long recargaMaxima;

    //MÉTODO SALDO ACTUAL
    public long saldo(Long clienteId) {
        verificarCliente(clienteId);
        return repositorio.saldoDe(clienteId);
    }

    //MÉTODO ÚLTIMOS MOVIMIENTOS (entre 1 y 100)
    public List<Movimiento> movimientos(Long clienteId, int limite) {
        verificarCliente(clienteId);
        return repositorio.movimientos(clienteId, Math.max(1, Math.min(limite, 100)));
    }

    //MÉTODO RECARGAR EL SALDO con tarjeta, PSE o Nequi (pasarela simulada: no mueve dinero real)
    public Recarga recargar(Long clienteId, long monto, MedioPago medio, Map<String, String> datos) {
        verificarCliente(clienteId);
        if (medio == MedioPago.SALDO) {
            throw new RecargaInvalidaException("No se puede recargar el saldo con el mismo saldo");
        }
        if (monto < recargaMinima || monto > recargaMaxima) {
            throw new RecargaInvalidaException(
                    "La recarga debe ser de " + pesos(recargaMinima) + " a " + pesos(recargaMaxima));
        }
        Map<String, String> recibidos = datos == null ? Map.of() : datos;
        ValidadorMedioPago.validar(medio, recibidos);

        PasarelaPago.Resultado resultado = pasarela.procesar(medio, recibidos, monto);
        if (resultado.estado() != EstadoPago.APROBADO) {
            return new Recarga(resultado.estado(), resultado.mensaje(), resultado.referencia(),
                    repositorio.saldoDe(clienteId), null);
        }
        Movimiento movimiento = repositorio.acreditar(clienteId, TipoMovimiento.RECARGA, monto, null,
                resultado.referencia(), "Recarga con " + resultado.detalle());
        return new Recarga(resultado.estado(), resultado.mensaje(), resultado.referencia(),
                movimiento.saldoPosterior, movimiento);
    }

    //MÉTODO PAGAR UNA RESERVA CON SALDO. Lanza SaldoInsuficienteException (409) si no alcanza.
    //Lo llama PagoServicio dentro de su transacción: si el pago falla después, el descuento se deshace.
    public Movimiento cobrarReserva(Long clienteId, Long reservaId, long monto, String referencia) {
        return repositorio.debitar(clienteId, monto, reservaId, referencia, "Pago de la reserva #" + reservaId);
    }

    //MÉTODO DEVOLVER AL SALDO LO PAGADO POR UNA RESERVA CANCELADA. Una reserva se reembolsa una sola vez.
    public void reembolsarReserva(Long clienteId, Long reservaId, long monto) {
        if (repositorio.existeMovimiento(reservaId, TipoMovimiento.REEMBOLSO)) {
            return;
        }
        repositorio.acreditar(clienteId, TipoMovimiento.REEMBOLSO, monto, reservaId,
                "REEM-" + reservaId, "Reembolso de la reserva #" + reservaId);
    }

    private void verificarCliente(Long clienteId) {
        if (clienteRepositorio.buscarPorId(clienteId).isEmpty()) {
            throw new BilleteraClienteNoEncontradoException(clienteId);
        }
    }

    private static String pesos(long valor) {
        return "$" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO")).format(valor);
    }
}
