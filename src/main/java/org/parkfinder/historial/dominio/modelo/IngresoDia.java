package org.parkfinder.historial.dominio.modelo;

import java.time.LocalDate;

/** Ingresos de un día del periodo, separados por origen (HU-24). */
public record IngresoDia(LocalDate fecha, double reservas, double estadias, double total) {
}
