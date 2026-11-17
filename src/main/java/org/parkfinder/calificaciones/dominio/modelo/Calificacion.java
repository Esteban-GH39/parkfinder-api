package org.parkfinder.calificaciones.dominio.modelo;

import lombok.Builder;

import java.time.LocalDateTime;

/** HU-13: la opinión de un cliente sobre un parqueadero (1 a 5 estrellas y un comentario opcional). */
@Builder
public class Calificacion {
    public Long id;
    public Long parqueaderoId;
    public Long clienteId;
    public String clienteNombre;
    public int estrellas;
    public String comentario;
    public LocalDateTime creadaEn;
}
