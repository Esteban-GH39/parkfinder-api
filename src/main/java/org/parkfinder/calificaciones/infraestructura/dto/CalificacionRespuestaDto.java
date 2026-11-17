package org.parkfinder.calificaciones.infraestructura.dto;

import org.parkfinder.calificaciones.dominio.modelo.Calificacion;

import java.time.LocalDateTime;

public record CalificacionRespuestaDto(
        Long id,
        Long parqueaderoId,
        String clienteNombre,
        int estrellas,
        String comentario,
        LocalDateTime creadaEn
) {
    public static CalificacionRespuestaDto de(Calificacion c) {
        return new CalificacionRespuestaDto(c.id, c.parqueaderoId, c.clienteNombre, c.estrellas, c.comentario,
                c.creadaEn);
    }
}
