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
        String nombrePropietario
) {
}
