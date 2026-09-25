package org.parkfinder.parqueaderos.infraestructura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ParqueaderoDto(
        @NotBlank(message = "El nombre es requerido")
        String nombre,
        @NotBlank(message = "La dirección es requerida")
        String direccion,
        @NotBlank(message = "La ubicación es requerida")
        String ubicacion,
        @NotNull(message = "La capacidad es requerida")
        int capacidad,
        @NotNull(message = "La tarifa es requerida")
        Double tarifa,
        @NotNull(message = "La hora de inicio es requerida")
        LocalTime horaInicio,
        @NotNull(message = "La hora final es requerida")
        LocalTime horaFinal,
        @NotBlank(message = "El nombre del propietario o representante es requerido")
        String nombrePropietario
) {


}
