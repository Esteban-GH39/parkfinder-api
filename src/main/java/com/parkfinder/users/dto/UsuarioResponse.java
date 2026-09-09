package com.parkfinder.users.dto;

import com.parkfinder.users.Rol;
import com.parkfinder.users.Usuario;

/** Respuesta pública de un usuario — nunca incluye la contraseña/hash. */
public record UsuarioResponse(Long id, String nombre, String email, Rol rol) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.id, usuario.nombre, usuario.email, usuario.rol);
    }
}
