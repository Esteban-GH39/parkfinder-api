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
        String rol = cliente.rol != null ? cliente.rol : Cliente.ROL_CLIENTE;
        Integer adminId = Cliente.ROL_ADMIN.equals(rol) ? crearAdmin(cliente) : null;
        ClienteEntity clienteEntity = ClienteEntity
                .builder()
                .nombre(cliente.nombre)
                .correo(cliente.correo)
                .contrasenaHash(cliente.contrasenaHash)
                .rol(rol)
                .adminId(adminId)
                .build();
        persist(clienteEntity);
    }

    // Un dueño necesita su fila en ADMIN: es a la que apuntan los parqueaderos (fk_parqueadero_admin).
    private Integer crearAdmin(Cliente cliente) {
        Number siguiente = (Number) getEntityManager()
                .createNativeQuery("select coalesce(max(id), 0) + 1 from admin")
                .getSingleResult();
        getEntityManager()
                .createNativeQuery("insert into admin (id, nombre, telefono, cedula, estado, fecharegistro) "
                        + "values (?1, ?2, ?3, ?4, true, current_date)")
                .setParameter(1, siguiente.intValue())
                .setParameter(2, cliente.nombre)
                .setParameter(3, cliente.telefono)
                .setParameter(4, cliente.cedula)
                .executeUpdate();
        return siguiente.intValue();
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return find("correo", correo).firstResultOptional().isPresent();
    }

    @Override
    public Optional<Cliente> buscarPorCorreo(String correo) {
        return find("correo", correo).firstResultOptional()
                .map(e -> Cliente.builder()
                        .id(e.id)
                        .nombre(e.nombre)
                        .correo(e.correo)
                        .contrasenaHash(e.contrasenaHash)
                        // filas creadas antes de existir la columna quedan con rol null
                        .rol(e.rol != null ? e.rol : Cliente.ROL_CLIENTE)
                        .adminId(e.adminId)
                        .build());
    }
}