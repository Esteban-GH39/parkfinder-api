package org.parkfinder.parqueaderos.dominio.modelo;

import lombok.Builder;

import java.time.LocalTime;

@Builder
public class Parqueadero {
    public Long id;
    public int fk_parqueadero_admin;
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
    public int cuposDisponibles;
    /** Parte de capacidadTotal que es de motos (el resto es de carros); null equivale a 0. */
    public Integer capacidadMotos;
    /** Parte de cuposDisponibles que es de motos; null equivale a 0. */
    public Integer cuposMotos;
    /** Tarifa por hora de la moto; null significa que paga la misma tarifa que el carro. */
    public Double tarifaHoraMoto;


    public int getCapacidadTotal() {
        return capacidadTotal;
    }

    public void setCuposDisponibles(int cuposDisponibles) {
        this.cuposDisponibles = cuposDisponibles;
    }
}
