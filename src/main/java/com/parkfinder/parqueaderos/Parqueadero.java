package com.parkfinder.parqueaderos;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Parqueadero extends PanacheEntity {
    public String nombre;
    public String zona;
    public String direccion;
    public Double latitud;
    public Double longitud;
    public String representante;
    public Integer capacidadTotal;
    public Integer cuposOcupados;
    public Double tarifaHora;
    public Double tarifaDia;

    public Integer getCuposDisponibles() {
        return capacidadTotal - cuposOcupados;
    }
}