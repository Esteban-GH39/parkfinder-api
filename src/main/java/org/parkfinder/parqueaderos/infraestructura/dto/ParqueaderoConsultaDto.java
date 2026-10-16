package org.parkfinder.parqueaderos.infraestructura.dto;

import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoConsulta;

import java.time.LocalTime;

/**
 * HU-19: un parqueadero registrado, tal como se devuelve por la API.
 *
 * Usa {@code horaFin} (nombre de la entidad) y no {@code horaFinal} (nombre del
 * DTO de registro y edición): es la forma en que el frontend ya lo lee.
 */
public record ParqueaderoConsultaDto(
        Long id,
        String nombre,
        String direccion,
        String zona,
        int capacidadTotal,
        Double tarifaHora,
        Double tarifaDia,
        Double tarifaNoche,
        LocalTime horaInicio,
        LocalTime horaFin,
        String nombrePropietario
) {

    public static ParqueaderoConsultaDto de(ParqueaderoConsulta p) {
        return new ParqueaderoConsultaDto(
                p.id,
                p.nombre,
                p.direccion,
                p.zona,
                p.capacidadTotal,
                p.tarifaHora,
                p.tarifaDia,
                p.tarifaNoche,
                p.horaInicio,
                p.horaFin,
                p.nombrePropietario);
    }
}
