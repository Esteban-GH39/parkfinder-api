package org.parkfinder.estadias.infraestructura.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Ingreso con código de reserva: se valida el código y se guarda la placa de cada vehículo. */
public record IngresoReservaDto(
        @NotNull(message = "La reserva es requerida")
        Long reservaId,
        @NotNull(message = "El parqueadero es requerido")
        Long parqueaderoId,
        @NotNull(message = "El id del administrador es requerido")
        Integer adminId,
        @NotEmpty(message = "Digita la placa de al menos un vehículo")
        List<@Valid VehiculoDto> vehiculos
) {
}
