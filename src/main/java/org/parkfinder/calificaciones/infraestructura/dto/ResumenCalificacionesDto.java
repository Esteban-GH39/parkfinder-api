package org.parkfinder.calificaciones.infraestructura.dto;

import org.parkfinder.calificaciones.dominio.modelo.ResumenCalificaciones;

import java.util.List;

public record ResumenCalificacionesDto(
        Double promedio,
        int total,
        List<CalificacionRespuestaDto> calificaciones
) {
    public static ResumenCalificacionesDto de(ResumenCalificaciones r) {
        return new ResumenCalificacionesDto(r.promedio, r.total,
                r.calificaciones.stream().map(CalificacionRespuestaDto::de).toList());
    }
}
