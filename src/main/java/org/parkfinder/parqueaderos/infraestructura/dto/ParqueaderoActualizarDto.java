package org.parkfinder.parqueaderos.infraestructura.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalTime;

/**
 * HU-19: datos de entrada para editar un parqueadero.
 *
 * Todos los campos son opcionales: los que lleguen en null se dejan como están,
 * para que la pantalla de edición pueda enviar solo lo que el usuario modificó.
 * Por eso los tipos son objeto ({@code Integer}, {@code Double}) y no primitivos.
 */
public record ParqueaderoActualizarDto(
        String nombre,
        String direccion,
        String zona,
        @Min(value = 0, message = "La capacidad no puede ser negativa")
        Integer capacidadTotal,
        @PositiveOrZero(message = "La tarifa por hora no puede ser negativa")
        Double tarifaHora,
        @PositiveOrZero(message = "La tarifa por día no puede ser negativa")
        Double tarifaDia,
        @PositiveOrZero(message = "La tarifa por noche no puede ser negativa")
        Double tarifaNoche,
        LocalTime horaInicio,
        LocalTime horaFinal,
        String nombrePropietario,
        @Min(value = 0, message = "Los cupos de motos no pueden ser negativos")
        Integer capacidadMotos,
        @PositiveOrZero(message = "La tarifa de moto no puede ser negativa")
        Double tarifaHoraMoto
) {
    /** Edición sin tocar los cupos ni la tarifa de motos (la forma que tenía antes de separar carros y motos). */
    public ParqueaderoActualizarDto(String nombre, String direccion, String zona, Integer capacidadTotal,
                                    Double tarifaHora, Double tarifaDia, Double tarifaNoche,
                                    LocalTime horaInicio, LocalTime horaFinal, String nombrePropietario) {
        this(nombre, direccion, zona, capacidadTotal, tarifaHora, tarifaDia, tarifaNoche, horaInicio, horaFinal,
                nombrePropietario, null, null);
    }
}
