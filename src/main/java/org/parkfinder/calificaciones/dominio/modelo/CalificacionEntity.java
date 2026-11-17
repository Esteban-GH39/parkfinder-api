package org.parkfinder.calificaciones.dominio.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CALIFICACIONES", uniqueConstraints = {
        @UniqueConstraint(name = "uk_calificacion_cliente_parqueadero", columnNames = {"clienteId", "parqueaderoId"})
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalificacionEntity extends PanacheEntity {
    public Long parqueaderoId;
    public Long clienteId;
    public int estrellas;
    @Column(length = 500)
    public String comentario;
    public LocalDateTime creadaEn;
}
