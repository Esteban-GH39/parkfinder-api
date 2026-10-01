package org.parkfinder.clientes.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.parkfinder.clientes.dominio.modelo.Cliente;
import org.parkfinder.clientes.dominio.repositorio.ClienteRepositorio;

@ApplicationScoped
public class ClienteServicio {

    @Inject
    ClienteRepositorio repositorio;

    public void registrarCliente(Cliente cliente) {
        repositorio.crearCliente(cliente);
    }

    public boolean correoYaRegistrado(String correo) {
        return repositorio.existePorCorreo(correo);
    }
}