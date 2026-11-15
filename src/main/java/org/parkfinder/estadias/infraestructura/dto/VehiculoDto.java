package org.parkfinder.estadias.infraestructura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;

public record VehiculoDto(
        @NotBlank(message = "La placa es requerida")
        String placa,
        @NotNull(message = "El tipo de vehículo es requerido (CARRO o MOTO)")
        TipoVehiculo tipoVehiculo
) {
}
