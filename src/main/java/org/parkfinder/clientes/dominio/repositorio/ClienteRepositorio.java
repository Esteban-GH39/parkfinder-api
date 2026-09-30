package org.parkfinder.clientes.dominio.repositorio;

import org.parkfinder.clientes.dominio.modelo.Cliente;

import java.util.Optional;

public interface ClienteRepositorio {
    void crearCliente(Cliente cliente);
    boolean existePorCorreo(String correo);
    Optional<Cliente> buscarPorCorreo(String correo);
}