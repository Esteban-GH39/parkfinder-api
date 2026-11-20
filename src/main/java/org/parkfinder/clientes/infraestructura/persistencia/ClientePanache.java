package org.parkfinder.clientes.infraestructura.persistencia;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.parkfinder.clientes.dominio.modelo.Cliente;
import org.parkfinder.clientes.dominio.modelo.ClienteEntity;
import org.parkfinder.clientes.dominio.repositorio.ClienteRepositorio;

import java.util.List;
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

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        ClienteEntity e = findById(id);
        return Optional.ofNullable(e).map(this::aPerfil);
    }

    @Override
    @Transactional
    public Optional<Cliente> actualizarPerfil(Long id, String nombre, Long telefono) {
        ClienteEntity e = findById(id);
        if (e == null) {
            return Optional.empty();
        }
        e.nombre = nombre;
        // Un dueño también tiene su fila en ADMIN: se mantiene alineada con la cuenta.
        if (Cliente.ROL_ADMIN.equals(e.rol) && e.adminId != null) {
            getEntityManager()
                    .createNativeQuery("update admin set nombre = ?1, telefono = coalesce(?2, telefono) where id = ?3")
                    .setParameter(1, nombre)
                    .setParameter(2, telefono)
                    .setParameter(3, e.adminId)
                    .executeUpdate();
        }
        return Optional.of(aPerfil(e));
    }

    @Override
    @Transactional
    public boolean actualizarContrasena(Long id, String nuevoHash) {
        ClienteEntity e = findById(id);
        if (e == null) {
            return false;
        }
        e.contrasenaHash = nuevoHash;
        return true;
    }

    // Arma el perfil; teléfono y cédula solo existen para los dueños (tabla ADMIN).
    private Cliente aPerfil(ClienteEntity e) {
        Long telefono = null;
        Long cedula = null;
        if (Cliente.ROL_ADMIN.equals(e.rol) && e.adminId != null) {
            List<?> filas = getEntityManager()
                    .createNativeQuery("select telefono, cedula from admin where id = ?1")
                    .setParameter(1, e.adminId)
                    .getResultList();
            if (!filas.isEmpty()) {
                Object[] fila = (Object[]) filas.get(0);
                telefono = fila[0] == null ? null : ((Number) fila[0]).longValue();
                cedula = fila[1] == null ? null : ((Number) fila[1]).longValue();
            }
        }
        return Cliente.builder()
                .id(e.id)
                .nombre(e.nombre)
                .correo(e.correo)
                .contrasenaHash(e.contrasenaHash)
                .rol(e.rol != null ? e.rol : Cliente.ROL_CLIENTE)
                .adminId(e.adminId)
                .telefono(telefono)
                .cedula(cedula)
                .build();
    }
}
