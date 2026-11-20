package org.parkfinder.clientes.infraestructura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** El correo y el rol no se cambian desde aquí. telefono se ignora en las cuentas de cliente. */
public record PerfilActualizarDto(
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        String nombre,

        Long telefono
) {
}
