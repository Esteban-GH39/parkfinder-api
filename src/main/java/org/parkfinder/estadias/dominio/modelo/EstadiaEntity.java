package org.parkfinder.estadias.dominio.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;

import java.time.LocalDateTime;

@Entity
@Table(name = "ESTADIAS", indexes = {
        @Index(name = "idx_estadias_placa_salida", columnList = "placa, salidaEn"),
        @Index(name = "idx_estadias_parqueadero_salida", columnList = "parqueaderoId, salidaEn")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadiaEntity extends PanacheEntity {
    public Long parqueaderoId;
    public Long reservaId;
    public String placa;
    @Enumerated(EnumType.STRING)
    public TipoVehiculo tipoVehiculo;
    public LocalDateTime ingresoEn;
    public LocalDateTime salidaEn;
    public Long minutos;
    public Double valorCobrado;
}
