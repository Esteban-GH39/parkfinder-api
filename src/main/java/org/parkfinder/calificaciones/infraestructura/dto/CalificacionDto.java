package org.parkfinder.calificaciones.infraestructura.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CalificacionDto(
        @NotNull(message = "El cliente es requerido")
        Long clienteId,

        @NotNull(message = "El parqueadero es requerido")
        Long parqueaderoId,

        @Min(value = 1, message = "La calificación debe ser de 1 a 5 estrellas")
        @Max(value = 5, message = "La calificación debe ser de 1 a 5 estrellas")
        int estrellas,

        // Opcional
        @Size(max = 500, message = "El comentario no puede superar 500 caracteres")
        String comentario
) {
}
