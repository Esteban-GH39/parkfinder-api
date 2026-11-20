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

    /** Perfil de la cuenta; vacío si no existe. */
    public Optional<Cliente> obtenerPerfil(Long id) {
        return repositorio.buscarPorId(id);
    }

    /** Cambia nombre (y teléfono si es dueño). Vacío si la cuenta no existe. */
    public Optional<Cliente> actualizarPerfil(Long id, String nombre, Long telefono) {
        return repositorio.actualizarPerfil(id, nombre.trim(), telefono);
    }

    public enum ResultadoCambioContrasena { CAMBIADA, NO_EXISTE, CONTRASENA_INCORRECTA }

    /** Exige la contraseña actual: así una sesión abierta y olvidada no basta para quedarse con la cuenta. */
    public ResultadoCambioContrasena cambiarContrasena(Long id, String contrasenaActual, String contrasenaNueva) {
        Optional<Cliente> cliente = repositorio.buscarPorId(id);
        if (cliente.isEmpty()) {
            return ResultadoCambioContrasena.NO_EXISTE;
        }
        if (!BcryptUtil.matches(contrasenaActual, cliente.get().contrasenaHash)) {
            return ResultadoCambioContrasena.CONTRASENA_INCORRECTA;
        }
        repositorio.actualizarContrasena(id, BcryptUtil.bcryptHash(contrasenaNueva));
        return ResultadoCambioContrasena.CAMBIADA;
    }
}
