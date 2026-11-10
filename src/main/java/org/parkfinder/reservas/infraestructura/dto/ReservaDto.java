package org.parkfinder.reservas.infraestructura.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** Cada tipo de vehículo lleva su propio contador; si no llega ninguno se reserva un cupo de carro. */
public record ReservaDto(
        @NotNull(message = "El cliente es obligatorio") Long clienteId,
        @NotNull(message = "El parqueadero es obligatorio") Long parqueaderoId,
        @NotNull(message = "La fecha y hora de llegada es obligatoria") LocalDateTime fechaHoraLlegada,
        @Min(value = 0, message = "Los cupos de carro no pueden ser negativos") Integer cantidadCarros,
        @Min(value = 0, message = "Los cupos de moto no pueden ser negativos") Integer cantidadMotos
) {
}
