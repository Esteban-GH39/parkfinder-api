package org.parkfinder.parqueaderos.dominio.modelo;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * HU-19: un renglón de la trazabilidad. Representa un campo que cambió en una
 * edición del parqueadero, con su valor anterior y el nuevo.
 */
@Builder
public class HistorialCambio {
    public Long parqueaderoId;
    public String campo;
    public String valorAnterior;
    public String valorNuevo;
    public LocalDateTime fecha;
}
