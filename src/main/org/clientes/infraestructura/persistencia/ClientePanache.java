package org.parkfinder.clientes.infraestructura.persistencia;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.parkfinder.clientes.dominio.modelo.Cliente;
import org.parkfinder.clientes.dominio.modelo.ClienteEntity;
import org.parkfinder.clientes.dominio.repositorio.ClienteRepositorio;

import java.util.Optional;

@ApplicationScoped
public class ClientePanache implements ClienteRepositorio, PanacheRepository<ClienteEntity> {

    @Override
    @Transactional
    public void crearCliente(Cliente cliente) {
        ClienteEntity clienteEntity = ClienteEntity
                .builder()
                .nombre(cliente.nombre)
                .correo(cliente.correo)
                .contrasenaHash(cliente.contrasenaHash)
                .build();
        persist(clienteEntity);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return find("correo", correo).firstResultOptional().isPresent();
    }

    @Override
    public Optional<Cliente> buscarPorCorreo(String correo) {
        return find("correo", correo).firstResultOptional()
                .map(e -> Cliente.builder()
                        .nombre(e.nombre)
                        .correo(e.correo)
                        .contrasenaHash(e.contrasenaHash)
                        .build());
    }
}