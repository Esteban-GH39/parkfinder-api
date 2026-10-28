package org.parkfinder.auth.infraestructura.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(
        @NotBlank(message = "El correo es requerido")
        String correo,

        @NotBlank(message = "La contraseña es requerida")
        String contrasena
) {
}