package org.parkfinder.comprobante.dominio.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.parkfinder.clientes.dominio.modelo.ClienteEntity;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoEntity;

import java.time.LocalDate;

@Entity
@Table(name="COMPROBANTE")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteEntity extends PanacheEntity {
    public LocalDate fechaGenerado;
    public Double valorPago;
    public String medioPago;
    public String estadoTransaccion;

    // LLAVE FORANEA HACIA CLIENTES(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_comprobante_cliente", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_comprobante_cliente"))
    public ClienteEntity cliente;

    // LLAVE FORANEA HACIA PARQUEADEROS(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_comprobante_parqueadero", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_comprobante_parqueadero"))
    public ParqueaderoEntity parqueadero;
}
