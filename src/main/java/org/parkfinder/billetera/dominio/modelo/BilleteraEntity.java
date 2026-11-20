package org.parkfinder.billetera.dominio.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/** Saldo actual de un cliente: una sola fila por cliente (se crea con la primera operación). */
@Entity
@Table(name = "BILLETERAS")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BilleteraEntity extends PanacheEntity {
    @Column(unique = true, nullable = false)
    public Long clienteId;
    public long saldo;
}
