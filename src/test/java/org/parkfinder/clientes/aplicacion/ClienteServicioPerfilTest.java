package org.parkfinder.clientes.aplicacion;

import io.quarkus.elytron.security.common.BcryptUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.parkfinder.clientes.aplicacion.ClienteServicio.ResultadoCambioContrasena;
import org.parkfinder.clientes.dominio.modelo.Cliente;
import org.parkfinder.clientes.dominio.repositorio.ClienteRepositorio;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Perfil y cambio de contraseña, sin levantar Quarkus ni la base de datos. */
class ClienteServicioPerfilTest {

    private ClienteRepositorio repositorio;
    private ClienteServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(ClienteRepositorio.class);
        servicio = new ClienteServicio();
        servicio.repositorio = repositorio;
        when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(Cliente.builder()
                .id(1L).nombre("Laura").correo("laura@correo.com").rol(Cliente.ROL_CLIENTE)
                .contrasenaHash(BcryptUtil.bcryptHash("Park1234")).build()));
    }

    @Test
    void cambiarContrasenaConLaActualCorrectaGuardaElNuevoHash() {
        assertEquals(ResultadoCambioContrasena.CAMBIADA, servicio.cambiarContrasena(1L, "Park1234", "Nueva5678"));

        ArgumentCaptor<String> hash = ArgumentCaptor.forClass(String.class);
        verify(repositorio).actualizarContrasena(org.mockito.ArgumentMatchers.eq(1L), hash.capture());
        assertTrue(BcryptUtil.matches("Nueva5678", hash.getValue()));
    }

    @Test
    void conLaContrasenaActualIncorrectaNoSeCambiaNada() {
        assertEquals(ResultadoCambioContrasena.CONTRASENA_INCORRECTA,
                servicio.cambiarContrasena(1L, "otra-cosa1", "Nueva5678"));
        verify(repositorio, never()).actualizarContrasena(anyLong(), anyString());
    }

    @Test
    void cuentaInexistente() {
        when(repositorio.buscarPorId(9L)).thenReturn(Optional.empty());

        assertEquals(ResultadoCambioContrasena.NO_EXISTE, servicio.cambiarContrasena(9L, "x", "Nueva5678"));
    }

    @Test
    void actualizarPerfilQuitaLosEspaciosDelNombre() {
        when(repositorio.actualizarPerfil(any(), anyString(), any()))
                .thenReturn(Optional.of(Cliente.builder().id(1L).nombre("Laura G").build()));

        servicio.actualizarPerfil(1L, "  Laura G  ", null);

        verify(repositorio).actualizarPerfil(1L, "Laura G", null);
    }
}
