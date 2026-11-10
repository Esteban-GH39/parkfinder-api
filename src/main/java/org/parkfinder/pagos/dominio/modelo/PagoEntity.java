package org.parkfinder.pagos.dominio.modelo;

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
@Table(name = "PAGOS")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoEntity extends PanacheEntity {
    public Long reservaId;
    @Enumerated(EnumType.STRING)
    public MedioPago medioPago;
    public Double monto;
    @Enumerated(EnumType.STRING)
    public EstadoPago estado;
    public String referencia;
    public String detalle;
    public String mensaje;
    public LocalDateTime creadoEn;
}
