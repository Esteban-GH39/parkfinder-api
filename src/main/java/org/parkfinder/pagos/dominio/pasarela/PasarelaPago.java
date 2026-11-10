package org.parkfinder.pagos.dominio.pasarela;

import org.parkfinder.pagos.dominio.modelo.EstadoPago;
import org.parkfinder.pagos.dominio.modelo.MedioPago;

import java.util.Map;

/** Puerto hacia la pasarela de pagos. Hoy solo existe la simulada: no mueve dinero real. */
public interface PasarelaPago {

    record Resultado(EstadoPago estado, String referencia, String detalle, String mensaje) {
    }

    Resultado procesar(MedioPago medio, Map<String, String> datos, double monto);
}
