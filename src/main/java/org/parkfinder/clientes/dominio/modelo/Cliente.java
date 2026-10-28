package org.parkfinder.clientes.dominio.modelo;

import lombok.Builder;

@Builder
public class Cliente {
    public static final String ROL_CLIENTE = "CLIENTE";
    public static final String ROL_ADMIN = "ADMIN";

    public Long id;
    public String nombre;
    public String correo;
    public String contrasenaHash;
    public String rol;
    public Integer adminId;
    // Solo para registrar a un dueño: datos que pide la tabla ADMIN
    public Long telefono;
    public Long cedula;
}