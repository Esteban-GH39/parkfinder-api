package org.parkfinder.clientes.infraestructura;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.parkfinder.clientes.aplicacion.ClienteServicio;
import org.parkfinder.clientes.dominio.modelo.Cliente;
import org.parkfinder.clientes.infraestructura.dto.ClienteDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Registro con rol: sin levantar Quarkus ni tocar la base de datos.
 */
class ClienteRecursosRolTest {

    private ClienteServicio servicio;
    private ClienteRecursos recursos;

    @BeforeEach
    void preparar() {
        servicio = mock(ClienteServicio.class);
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/clientes");
        recursos = new ClienteRecursos();
        recursos.clienteServicio = servicio;
        recursos.uriInfo = uriInfo;
    }

    @Test
    void registrar_sinRol_creaUnCliente() {
        Response respuesta = recursos.registrar(
                new ClienteDto("Laura", "laura@correo.com", "Park1234", null, null, null));

        assertEquals(201, respuesta.getStatus());
        ArgumentCaptor<Cliente> creado = ArgumentCaptor.forClass(Cliente.class);
        verify(servicio).registrarCliente(creado.capture());
        assertEquals(Cliente.ROL_CLIENTE, creado.getValue().rol);
    }

    @Test
    void registrar_comoAdminConTelefonoYCedula_creaUnDueno() {
        Response respuesta = recursos.registrar(
                new ClienteDto("Antonio", "antonio@correo.com", "Park1234", "ADMIN", 3104770834L, 1056348935L));

        assertEquals(201, respuesta.getStatus());
        ArgumentCaptor<Cliente> creado = ArgumentCaptor.forClass(Cliente.class);
        verify(servicio).registrarCliente(creado.capture());
        assertEquals(Cliente.ROL_ADMIN, creado.getValue().rol);
        assertEquals(3104770834L, creado.getValue().telefono);
        assertEquals(1056348935L, creado.getValue().cedula);
    }

    @Test
    void registrar_comoAdminSinTelefono_devuelve400YNoGuarda() {
        Response respuesta = recursos.registrar(
                new ClienteDto("Antonio", "antonio@correo.com", "Park1234", "ADMIN", null, 1056348935L));

        assertEquals(400, respuesta.getStatus());
        verify(servicio, never()).registrarCliente(any());
    }
}
