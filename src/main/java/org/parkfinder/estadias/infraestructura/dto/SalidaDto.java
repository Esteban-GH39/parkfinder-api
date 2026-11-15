package org.parkfinder.estadias.infraestructura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Liberar cupo: el administrador digita la placa del vehículo que sale. */
public record SalidaDto(
        @NotNull(message = "El parqueadero es requerido")
        Long parqueaderoId,
        @NotNull(message = "El id del administrador es requerido")
        Integer adminId,
        @NotBlank(message = "La placa es requerida")
        String placa
) {
}
