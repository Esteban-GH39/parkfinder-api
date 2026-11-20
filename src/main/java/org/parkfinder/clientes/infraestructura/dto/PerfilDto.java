package org.parkfinder.clientes.infraestructura.dto;

import org.parkfinder.clientes.dominio.modelo.Cliente;

/** Perfil que ve el usuario. telefono y cedula solo vienen en las cuentas ADMIN (dueños). */
public record PerfilDto(Long id, String nombre, String correo, String rol, Integer adminId,
                        Long telefono, Long cedula) {

    public static PerfilDto de(Cliente c) {
        return new PerfilDto(c.id, c.nombre, c.correo, c.rol, c.adminId, c.telefono, c.cedula);
    }
}
