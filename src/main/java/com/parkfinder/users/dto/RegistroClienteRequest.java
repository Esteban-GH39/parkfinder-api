package com.parkfinder.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Datos de entrada de HU-01 (registro de cliente): nombre, correo y contraseña.
 * Regla de contraseña del criterio de aceptación: mínimo 8 caracteres, con al
 * menos una letra y un número.
 */
public record RegistroClienteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                message = "La contraseña debe tener mínimo 8 caracteres, con al menos una letra y un número"
        )
        String password
) {
}
