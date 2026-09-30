package org.parkfinder.parqueaderos.dominio.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name="PARQUEADEROS")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParqueaderoEntity extends PanacheEntity {
    public String nombre;
    public String direccion;
    public String zona;
    public int capacidadTotal;
    public Double tarifaHora;
    public Double tarifaDia;
    public Double tarifaNoche;
    public LocalTime horaInicio;
    public LocalTime horaFin;
    public  String nombrePropietario;
}
