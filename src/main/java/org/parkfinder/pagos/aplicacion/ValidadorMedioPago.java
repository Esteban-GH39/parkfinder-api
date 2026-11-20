package org.parkfinder.pagos.aplicacion;

import org.parkfinder.pagos.dominio.excepcion.PagoInvalidoException;
import org.parkfinder.pagos.dominio.modelo.MedioPago;

import java.time.YearMonth;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Valida el formato de los datos que pide cada medio de pago; cualquier dato con buen formato se acepta.
 * Lo usan el pago de una reserva (HU-07) y la recarga de la billetera.
 */
public final class ValidadorMedioPago {

    private static final Pattern VENCIMIENTO = Pattern.compile("^(0[1-9]|1[0-2])/(\\d{2})$");

    private ValidadorMedioPago() {
    }

    public static void validar(MedioPago medio, Map<String, String> datos) {
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
            case SALDO -> {
                // no pide datos
            }
        }
    }

    private static void validarVencimiento(String vencimiento) {
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
