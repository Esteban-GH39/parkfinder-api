package org.parkfinder.pagos.infraestructura.dto;

import jakarta.validation.constraints.NotNull;
import org.parkfinder.pagos.dominio.modelo.MedioPago;

import java.util.Map;

/** "datos" trae los campos que pide el medio: ver {@link MedioPago#camposRequeridos}. */
public record PagoDto(
        @NotNull(message = "La reserva es obligatoria") Long reservaId,
        @NotNull(message = "El medio de pago es obligatorio") MedioPago medioPago,
        Map<String, String> datos
) {
}
