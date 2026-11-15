package org.parkfinder.estadias.infraestructura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;

/** Ingreso directo: el administrador digita la placa; la hora la pone el sistema. */
public record IngresoDto(
        @NotNull(message = "El parqueadero es requerido")
        Long parqueaderoId,
        @NotNull(message = "El id del administrador es requerido")
        Integer adminId,
        @NotBlank(message = "La placa es requerida")
        String placa,
        @NotNull(message = "El tipo de vehículo es requerido (CARRO o MOTO)")
        TipoVehiculo tipoVehiculo
) {
}
