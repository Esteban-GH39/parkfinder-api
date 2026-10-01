package org.parkfinder.clientes.dominio.modelo;

import lombok.Builder;

@Builder
public class Cliente {
    public String nombre;
    public String correo;
    public String contrasenaHash;
}