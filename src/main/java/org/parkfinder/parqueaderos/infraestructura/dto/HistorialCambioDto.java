package org.parkfinder.parqueaderos.infraestructura.dto;

import org.parkfinder.parqueaderos.dominio.modelo.HistorialCambio;

import java.time.LocalDateTime;

/** HU-19: un renglón de la trazabilidad, tal como se devuelve por la API. */
public record HistorialCambioDto(
        String campo,
        String valorAnterior,
        String valorNuevo,
        LocalDateTime fecha
) {

    public static HistorialCambioDto de(HistorialCambio cambio) {
        return new HistorialCambioDto(
                cambio.campo,
                cambio.valorAnterior,
                cambio.valorNuevo,
                cambio.fecha);
    }
}
