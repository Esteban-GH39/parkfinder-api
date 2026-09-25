package org.parkfinder.parqueaderos.dominio.modelo;

import lombok.Builder;

import java.time.LocalTime;

@Builder
public class Parqueadero {
    public String nombre;
    public String direccion;
    public String ubicacion;
    public int capacidadTotal;
    public Double tarifa;
    public LocalTime horaInicio;
    public LocalTime horaFin;
    public String nombrePropietario;
}
