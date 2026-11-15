package org.parkfinder.parqueaderos.infraestructura;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkfinder.parqueaderos.aplicacion.ParqueaderoServicio;
import org.parkfinder.parqueaderos.infraestructura.dto.ActualizarCuposDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * HU-21: pruebas del endpoint PATCH /parqueaderos/{id}/cuposDisponibles
 * sin levantar Quarkus ni tocar la base de datos.
 */
class ParqueaderoRecursosHU21Test {

    private ParqueaderoServicio servicio;
    private ParqueaderoRecursos recursos;

    @BeforeEach
    void preparar() {
        servicio = mock(ParqueaderoServicio.class);
        recursos = new ParqueaderoRecursos();
        recursos.parqueaderoServicio = servicio;
    }

    @Test
    void testActualizarCuposExitoso() {
        Response respuesta = recursos.actualizarCupos(1L, new ActualizarCuposDto(1, 15, null));

        assertEquals(200, respuesta.getStatus());
        verify(servicio).actualizarCuposManualmente(1L, 1, 15, null);
    }

    @Test
    void testActualizarCuposSuperaCapacidadDevuelve400() {
        doThrow(new IllegalArgumentException("supera la capacidad"))
                .when(servicio).actualizarCuposManualmente(1L, 1, 999, null);

        Response respuesta = recursos.actualizarCupos(1L, new ActualizarCuposDto(1, 999, null));

        assertEquals(400, respuesta.getStatus());
    }

    @Test
    void testActualizarCuposDeOtroAdminDevuelve403() {
        doThrow(new SecurityException("sin permisos"))
                .when(servicio).actualizarCuposManualmente(1L, 2, 10, null);

        Response respuesta = recursos.actualizarCupos(1L, new ActualizarCuposDto(2, 10, null));

        assertEquals(403, respuesta.getStatus());
    }
}
