package org.parkfinder.clientes.infraestructura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambioContrasenaDto(
        @NotBlank(message = "La contraseña actual es requerida")
        String contrasenaActual,

        @NotBlank(message = "La contraseña nueva es requerida")
        @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres, con al menos una letra y un número"
        )
        String contrasenaNueva
) {
}
