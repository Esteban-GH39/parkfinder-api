package org.parkfinder.pagos.infraestructura;

import jakarta.enterprise.context.ApplicationScoped;
import org.parkfinder.pagos.dominio.modelo.EstadoPago;
import org.parkfinder.pagos.dominio.modelo.MedioPago;
import org.parkfinder.pagos.dominio.pasarela.PasarelaPago;

import java.util.Map;
import java.util.UUID;

/**
 * HU-07: pasarela de mentira. Aprueba cualquier pago con los datos completos.
 * Para poder probar el rechazo (HU-08), una tarjeta que termine en 0000 lo rechaza.
 * Nunca se conserva el número completo: solo los últimos dígitos, para mostrarlos.
 */
@ApplicationScoped
public class PasarelaSimulada implements PasarelaPago {

    static final String TARJETA_RECHAZADA = "0000";

    @Override
    public Resultado procesar(MedioPago medio, Map<String, String> datos, double monto) {
        String referencia = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String detalle = detalle(medio, datos);

        if (medio == MedioPago.TARJETA && soloDigitos(datos.get("numeroTarjeta")).endsWith(TARJETA_RECHAZADA)) {
            return new Resultado(EstadoPago.RECHAZADO, referencia, detalle, "Fondos insuficientes (simulado)");
        }
        return new Resultado(EstadoPago.APROBADO, referencia, detalle, "Pago aprobado (simulado)");
    }

    private String detalle(MedioPago medio, Map<String, String> datos) {
        return switch (medio) {
            case TARJETA -> {
                String digitos = soloDigitos(datos.get("numeroTarjeta"));
                String ultimos = digitos.length() > 4 ? digitos.substring(digitos.length() - 4) : digitos;
                yield "Tarjeta **** " + ultimos;
            }
            case PSE -> "PSE - " + datos.get("banco").trim();
            case NEQUI -> "Nequi - " + datos.get("celular").trim();
        };
    }

    private String soloDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }
}
