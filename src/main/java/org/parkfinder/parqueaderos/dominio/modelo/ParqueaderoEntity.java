package org.parkfinder.parqueaderos.dominio.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
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
    @Column(name = "fk_parqueadero_admin")
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
    public  String nombrePropietario;
    public int cuposDisponibles;
    /** Parte de capacidadTotal que es de motos (el resto es de carros); null equivale a 0. */
    public Integer capacidadMotos;
    /** Parte de cuposDisponibles que es de motos; null equivale a 0. */
    public Integer cuposMotos;
    /** Tarifa por hora de la moto; null significa que paga la misma tarifa que el carro. */
    public Double tarifaHoraMoto;
}
