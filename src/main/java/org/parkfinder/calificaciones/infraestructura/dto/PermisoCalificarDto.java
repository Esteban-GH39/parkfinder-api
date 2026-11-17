package org.parkfinder.calificaciones.infraestructura.dto;

/** Si el cliente puede calificar ahora el parqueadero, y si ya lo hizo antes. */
public record PermisoCalificarDto(boolean puede, boolean yaCalifico) {
}
