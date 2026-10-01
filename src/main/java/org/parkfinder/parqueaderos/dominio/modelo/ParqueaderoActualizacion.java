package org.parkfinder.parqueaderos.dominio.modelo;

import lombok.Builder;

import java.time.LocalTime;

/**
 * HU-19: datos de una edición de parqueadero.
 *
 * A diferencia de {@link Parqueadero}, todos los campos admiten null: un campo
 * en null significa "no cambiar", para que la pantalla de edición pueda enviar
 * únicamente lo que el usuario modificó.
 */
@Builder
public class ParqueaderoActualizacion {
    public String nombre;
    public String direccion;
    public String zona;
    public Integer capacidadTotal;
    public Double tarifaHora;
    public Double tarifaDia;
    public Double tarifaNoche;
    public LocalTime horaInicio;
    public LocalTime horaFin;
    public String nombrePropietario;
}
