package org.parkfinder.clientes.infraestructura.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClienteDto(
        @NotBlank(message = "El nombre es requerido")
        String nombre,

        @NotBlank(message = "El correo es requerido")
        @Email(message = "El correo no tiene un formato válido")
        String correo,

        @NotBlank(message = "La contraseña es requerida")
        @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres, con al menos una letra y un número"
        )
        String contrasena
) {
}