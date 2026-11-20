package org.parkfinder.pagos.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.parkfinder.billetera.aplicacion.BilleteraServicio;
import org.parkfinder.comprobante.aplicacion.ComprobanteServicio;
import org.parkfinder.comprobante.dominio.modelo.Comprobante;
import org.parkfinder.pagos.dominio.excepcion.PagoInvalidoException;
import org.parkfinder.pagos.dominio.excepcion.PagoNoPermitidoException;
import org.parkfinder.pagos.dominio.modelo.EstadoPago;
import org.parkfinder.pagos.dominio.modelo.MedioPago;
import org.parkfinder.pagos.dominio.modelo.Pago;
import org.parkfinder.pagos.dominio.pasarela.PasarelaPago;
import org.parkfinder.pagos.dominio.repositorio.PagoRepositorio;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class PagoServicio {

    @Inject
    PagoRepositorio repositorio;

    @Inject
    ReservaRepositorio reservaRepositorio;

    @Inject
    ComprobanteServicio comprobanteServicio;

    @Inject
    PasarelaPago pasarela;

    @Inject
    BilleteraServicio billetera;

    /** Lo que cuesta cada reserva, sin importar cuántos cupos ni de qué tipo. */
    @ConfigProperty(name = "parkfinder.pago.costo-reserva", defaultValue = "3000")
    double costoReserva;

    /** Rechazos que tolera una reserva: al llegar a este número se cancela y el cupo se libera (HU-08). */
    @ConfigProperty(name = "parkfinder.pago.max-intentos", defaultValue = "3")
    int maxIntentos;

    //MÉTODO PAGAR UNA RESERVA RETENIDA (HU-07). Un rechazo también se guarda y permite reintentar hasta
    //agotar los intentos (HU-08): entonces la reserva se cancela y el cupo vuelve al parqueadero.
    //Si se aprueba, la reserva queda confirmada y se genera su comprobante: su id es el código de reserva.
    //Todo en una sola transacción: si algo falla no queda un pago aprobado sin reserva confirmada ni código.
    @Transactional
    public Pago pagar(Long reservaId, MedioPago medio, Map<String, String> datos) {
        Map<String, String> recibidos = datos == null ? Map.of() : datos;
        validarDatos(medio, recibidos);

        // Lanza ReservaNoEncontradaException si no existe; marca EXPIRADA si la retención venció
        Reserva reserva = reservaRepositorio.obtenerPorId(reservaId);
        if (reserva.estado != EstadoReserva.RETENIDA) {
            throw new PagoNoPermitidoException("La reserva ya no está retenida (estado " + reserva.estado + ")");
        }
        if (repositorio.existeAprobado(reservaId)) {
            throw new PagoNoPermitidoException("La reserva ya tiene un pago aprobado");
        }

        // Con saldo no hay pasarela: se descuenta de la billetera. Si no alcanza, lanza SaldoInsuficienteException
        // (409) y no se guarda ningún pago, así que no cuenta como intento rechazado (HU-08).
        PasarelaPago.Resultado resultado = medio == MedioPago.SALDO
                ? cobrarDelSaldo(reserva)
                : pasarela.procesar(medio, recibidos, costoReserva);
        Pago pago = repositorio.guardar(Pago
                .builder()
                .reservaId(reservaId)
                .medioPago(medio)
                .monto(costoReserva)
                .estado(resultado.estado())
                .referencia(resultado.referencia())
                .detalle(resultado.detalle())
                .mensaje(resultado.mensaje())
                .creadoEn(LocalDateTime.now())
                .build());
        // Pago aprobado = reserva confirmada: deja de vencer y el cupo sigue ocupado
        if (pago.estado == EstadoPago.APROBADO) {
            reservaRepositorio.confirmar(reservaId);
            Comprobante comprobante = comprobanteServicio.generar(Comprobante
                    .builder()
                    .fechaGenerado(LocalDate.now())
                    .valorPago(costoReserva)
                    .medioPago(medio.name())
                    .estadoTransaccion("APROBADO")
                    .fk_comprobante_cliente(reserva.clienteId)
                    .fk_comprobante_parqueadero(reserva.parqueaderoId)
                    .reservaId(reservaId)
                    .build());
            pago.comprobanteId = comprobante.idComprobante;
        } else {
            long restantes = maxIntentos - repositorio.contarRechazados(reservaId);
            pago.intentosRestantes = (int) Math.max(0, restantes);
            if (restantes <= 0) {
                reservaRepositorio.cancelar(reservaId);
                pago.reservaCancelada = true;
            }
        }
        return pago;
    }

    private PasarelaPago.Resultado cobrarDelSaldo(Reserva reserva) {
        String referencia = "SALDO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        billetera.cobrarReserva(reserva.clienteId, reserva.id, Math.round(costoReserva), referencia);
        return new PasarelaPago.Resultado(EstadoPago.APROBADO, referencia, "Saldo ParkFinder",
                "Pago aprobado con saldo");
    }

    private void validarDatos(MedioPago medio, Map<String, String> datos) {
        ValidadorMedioPago.validar(medio, datos);
    }
}
