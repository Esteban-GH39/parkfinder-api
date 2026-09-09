package com.parkfinder.users;

import java.time.Instant;

import org.mindrot.jbcrypt.BCrypt;

import com.parkfinder.users.dto.RegistroClienteRequest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UsuarioService {

    @Transactional
    public Usuario registrarCliente(RegistroClienteRequest request) {
        if (Usuario.buscarPorEmail(request.email()) != null) {
            throw new EmailYaRegistradoException(request.email());
        }

        Usuario usuario = new Usuario();
        usuario.nombre = request.nombre();
        usuario.email = request.email();
        usuario.passwordHash = BCrypt.hashpw(request.password(), BCrypt.gensalt());
        usuario.rol = Rol.CLIENTE;
        usuario.creadoEn = Instant.now();
        usuario.persist();

        return usuario;
    }
}
