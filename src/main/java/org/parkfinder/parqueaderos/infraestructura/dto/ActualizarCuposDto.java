package org.parkfinder.parqueaderos.infraestructura.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActualizarCuposDto(
        @NotNull(message = "El ID del administrador es requerido")
        Integer id,

        @NotNull(message = "La cantidad de cupos es requerida")
        @Min(value = 0, message = "Los cupos disponibles no pueden ser negativos")
        Integer cuposDisponibles
) {
}
