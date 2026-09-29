package org.parkfinder.parqueaderos.dominio.modelo;

import lombok.Builder;

import java.time.LocalTime;

@Builder
public class Parqueadero {
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
