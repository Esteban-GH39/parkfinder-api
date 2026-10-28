package org.parkfinder.reservas.infraestructura.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservaDto(
        @NotNull(message = "El cliente es obligatorio") Long clienteId,
        @NotNull(message = "El parqueadero es obligatorio") Long parqueaderoId,
        @NotNull(message = "La fecha y hora de llegada es obligatoria") LocalDateTime fechaHoraLlegada
) {
}
