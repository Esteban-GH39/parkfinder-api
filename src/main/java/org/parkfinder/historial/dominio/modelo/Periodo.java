package org.parkfinder.historial.dominio.modelo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

/** Periodo por el que el dueño filtra el historial de reservas e ingresos (HU-24). */
public enum Periodo {
    /** Solo el día de la fecha. */
    DIA,
    /** De lunes a domingo de la semana que contiene la fecha. */
    SEMANA,
    /** Del 1 al último día del mes que contiene la fecha. */
    MES;

    /** Primer y último día (ambos incluidos) del periodo que contiene la fecha. */
    public Rango rango(LocalDate fecha) {
        return switch (this) {
            case DIA -> new Rango(fecha, fecha);
            case SEMANA -> new Rango(
                    fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                    fecha.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)));
            case MES -> new Rango(fecha.withDayOfMonth(1), fecha.with(TemporalAdjusters.lastDayOfMonth()));
        };
    }

    /** Convierte el texto del query param ("dia", "SEMANA"...); null si no corresponde a ninguno. */
    public static Periodo de(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String normalizado = texto.trim().toUpperCase(Locale.ROOT).replace('Í', 'I');
        for (Periodo p : values()) {
            if (p.name().equals(normalizado)) {
                return p;
            }
        }
        return null;
    }

    /** Días del periodo, ambos extremos incluidos. */
    public record Rango(LocalDate desde, LocalDate hasta) {
    }
}
