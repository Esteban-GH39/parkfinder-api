package org.parkfinder.billetera.infraestructura.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.parkfinder.pagos.dominio.modelo.MedioPago;

import java.util.Map;

/** "datos" trae los campos que pide el medio: ver {@link MedioPago#camposRequeridos}. SALDO no sirve para recargar. */
public record RecargaDto(
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor que cero")
        Long monto,

        @NotNull(message = "El medio de pago es obligatorio")
        MedioPago medioPago,

        Map<String, String> datos
) {
}
