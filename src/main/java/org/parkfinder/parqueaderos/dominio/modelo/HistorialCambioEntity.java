package org.parkfinder.parqueaderos.dominio.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "HISTORIAL_CAMBIOS")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCambioEntity extends PanacheEntity {
    public Long parqueaderoId;
    public String campo;
    public String valorAnterior;
    public String valorNuevo;
    public LocalDateTime fecha;
}
