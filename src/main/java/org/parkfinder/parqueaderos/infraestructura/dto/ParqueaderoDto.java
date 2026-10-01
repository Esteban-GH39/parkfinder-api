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
        String zona,
        @NotNull(message = "La capacidad es requerida")
        int capacidadTotal,
        @NotNull(message = "La tarifa hora es requerida")
        Double tarifaHora,
        @NotNull(message = "La tarifa dia es requerida")
        Double tarifaDia,
        @NotNull(message = "La tarifa noche es requerida")
        Double tarifaNoche,
        @NotNull(message = "La hora de inicio es requerida")
        LocalTime horaInicio,
        @NotNull(message = "La hora final es requerida")
        LocalTime horaFinal,
        @NotBlank(message = "El nombre del propietario o representante es requerido")
        String nombrePropietario
) {


}
