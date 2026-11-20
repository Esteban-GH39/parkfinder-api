package org.parkfinder.clientes.dominio.repositorio;

import org.parkfinder.clientes.dominio.modelo.Cliente;

import java.util.Optional;

public interface ClienteRepositorio {
    void crearCliente(Cliente cliente);
    boolean existePorCorreo(String correo);
    Optional<Cliente> buscarPorCorreo(String correo);

    /** Perfil completo: si la cuenta es ADMIN incluye teléfono y cédula de su fila en la tabla ADMIN. */
    Optional<Cliente> buscarPorId(Long id);

    /** Cambia el nombre y, solo si la cuenta es ADMIN, el teléfono. Devuelve el perfil actualizado. */
    Optional<Cliente> actualizarPerfil(Long id, String nombre, Long telefono);

    /** Guarda el nuevo hash de la contraseña. false si la cuenta no existe. */
    boolean actualizarContrasena(Long id, String nuevoHash);
}
