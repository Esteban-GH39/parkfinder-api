package org.parkfinder.parqueaderos.dominio.modelo;

import lombok.Builder;

import java.time.LocalTime;

/**
 * HU-19: un parqueadero ya registrado, con su id.
 *
 * {@link Parqueadero} no lleva id porque se usa para crear; para editar hace
 * falta poder consultar un parqueadero existente y saber cuál es.
 */
@Builder
public class ParqueaderoConsulta {
    public Long id;
    public String nombre;
    public String direccion;
    public String zona;
    public int capacidadTotal;
    public Double tarifaHora;
    public Double tarifaDia;
    public Double tarifaNoche;
    public LocalTime horaInicio;
    public LocalTime horaFin;
    public String nombrePropietario;
}
