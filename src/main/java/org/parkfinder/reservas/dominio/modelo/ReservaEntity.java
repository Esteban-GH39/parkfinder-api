package org.parkfinder.reservas.dominio.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "RESERVAS")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaEntity extends PanacheEntity {
    public Long clienteId;
    public Long parqueaderoId;
    public LocalDateTime fechaHoraLlegada;
    @Enumerated(EnumType.STRING)
    public EstadoReserva estado;
    public LocalDateTime creadaEn;
    public LocalDateTime expiraEn;
}
