package org.parkfinder.clientes.dominio.modelo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CLIENTES")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEntity extends PanacheEntity {
    public String nombre;

    @Column(unique = true)
    public String correo;

    public String contrasenaHash;
}