package org.parkfinder.calificaciones.dominio.modelo;

import lombok.Builder;

import java.util.List;

/** HU-13: lo que muestra el detalle del parqueadero (HU-05): promedio, cantidad y comentarios. */
@Builder
public class ResumenCalificaciones {
    /** Promedio de estrellas con un decimal; null mientras no haya calificaciones. */
    public Double promedio;
    public int total;
    /** Más recientes primero. Incluye las calificaciones sin comentario (comentario null). */
    public List<Calificacion> calificaciones;
}
