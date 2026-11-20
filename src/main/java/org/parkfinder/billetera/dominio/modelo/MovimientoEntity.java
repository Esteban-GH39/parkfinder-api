package org.parkfinder.billetera.dominio.modelo;

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
@Table(name = "BILLETERA_MOVIMIENTOS")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoEntity extends PanacheEntity {
    public Long clienteId;
    @Enumerated(EnumType.STRING)
    public TipoMovimiento tipo;
    public long monto;
    public long saldoPosterior;
    public Long reservaId;
    public String referencia;
    public String detalle;
    public LocalDateTime creadoEn;
}
