package org.parkfinder.pagos.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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
import java.time.YearMonth;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class PagoServicio {

    private static final Pattern VENCIMIENTO = Pattern.compile("^(0[1-9]|1[0-2])/(\\d{2})$");

    @Inject
    PagoRepositorio repositorio;

    @Inject
    ReservaRepositorio reservaRepositorio;

    @Inject
    ComprobanteServicio comprobanteServicio;

    @Inject
    PasarelaPago pasarela;

    /** Lo que cuesta cada reserva, sin importar cuántos cupos ni de qué tipo. */
    @ConfigProperty(name = "parkfinder.pago.costo-reserva", defaultValue = "3000")
    double costoReserva;

    //MÉTODO PAGAR UNA RESERVA RETENIDA (HU-07). Un rechazo también se guarda y no impide reintentar.
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

        PasarelaPago.Resultado resultado = pasarela.procesar(medio, recibidos, costoReserva);
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
        }
        return pago;
    }

    /** Valida el formato de los datos de cada medio; cualquier dato con buen formato se acepta. */
    private void validarDatos(MedioPago medio, Map<String, String> datos) {
        for (String campo : medio.camposRequeridos) {
            String valor = datos.get(campo);
            if (valor == null || valor.isBlank()) {
                throw new PagoInvalidoException("Falta el dato '" + campo + "' para pagar con " + medio.etiqueta);
            }
        }
        switch (medio) {
            case TARJETA -> {
                if (!soloDigitos(datos.get("numeroTarjeta")).matches("\\d{16}")) {
                    throw new PagoInvalidoException("El número de la tarjeta debe tener 16 dígitos");
                }
                validarVencimiento(datos.get("vencimiento").trim());
                if (!datos.get("cvv").trim().matches("\\d{3,4}")) {
                    throw new PagoInvalidoException("El CVV debe tener 3 o 4 dígitos");
                }
            }
            case PSE -> {
                if (!datos.get("documento").trim().matches("\\d{6,10}")) {
                    throw new PagoInvalidoException("El documento debe tener entre 6 y 10 dígitos");
                }
            }
            case NEQUI -> {
                if (!datos.get("celular").trim().matches("3\\d{9}")) {
                    throw new PagoInvalidoException("El celular debe tener 10 dígitos y empezar por 3");
                }
            }
        }
    }

    private void validarVencimiento(String vencimiento) {
        Matcher m = VENCIMIENTO.matcher(vencimiento);
        if (!m.matches()) {
            throw new PagoInvalidoException("El vencimiento debe tener el formato MM/AA");
        }
        YearMonth vence = YearMonth.of(2000 + Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1)));
        if (vence.isBefore(YearMonth.now())) {
            throw new PagoInvalidoException("La tarjeta está vencida");
        }
    }

    private static String soloDigitos(String texto) {
        return texto.replaceAll("[\\s-]", "");
    }
}
