package org.parkfinder.clientes.aplicacion;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.parkfinder.clientes.dominio.modelo.Cliente;
import org.parkfinder.clientes.dominio.repositorio.ClienteRepositorio;

import java.util.Optional;

@ApplicationScoped
public class ClienteServicio {

    private static final String HASH_FICTICIO = BcryptUtil.bcryptHash("contrasena-ficticia-1");

    @Inject
    ClienteRepositorio repositorio;

    public void registrarCliente(Cliente cliente) {
        repositorio.crearCliente(cliente);
    }

    public boolean correoYaRegistrado(String correo) {
        return repositorio.existePorCorreo(correo);
    }

    public Optional<Cliente> autenticar(String correo, String contrasena) {
        Optional<Cliente> cliente = repositorio.buscarPorCorreo(correo);
        String hash = cliente.map(c -> c.contrasenaHash).orElse(HASH_FICTICIO);
        boolean coincide = BcryptUtil.matches(contrasena, hash);
        return cliente.filter(c -> coincide);
    }
}