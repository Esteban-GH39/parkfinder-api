package org.parkfinder.reservas.infraestructura;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkfinder.reservas.aplicacion.ReservaServicio;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.ReservaDetalle;
import org.parkfinder.reservas.infraestructura.dto.ReservaDetalleDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Vista del dueño: pruebas del recurso REST sin levantar Quarkus ni tocar la base de datos.
 */
class ReservaRecursosDuenoTest {

    private ReservaServicio servicio;
    private ReservaRecursos recursos;

    @BeforeEach
    void preparar() {
        servicio = mock(ReservaServicio.class);
        recursos = new ReservaRecursos();
        recursos.reservaServicio = servicio;
    }

    @Test
    void listarPorAdmin_devuelve200ConLasReservasDelDueno() {
        LocalDateTime llegada = LocalDateTime.of(2026, 11, 2, 10, 0);
        when(servicio.listarPorAdmin(1)).thenReturn(List.of(ReservaDetalle.builder()
                .id(9L)
                .parqueaderoId(251L)
                .parqueaderoNombre("Parqueadero Público Proyectar")
                .clienteNombre("Laura Gómez")
                .clienteCorreo("cliente1@parkfinder.com")
                .fechaHoraLlegada(llegada)
                .estado(EstadoReserva.RETENIDA)
                .creadaEn(llegada.minusDays(1))
                .expiraEn(llegada.minusDays(1).plusMinutes(10))
                .build()));

        Response respuesta = recursos.listarPorAdmin(1);

        assertEquals(200, respuesta.getStatus());
        @SuppressWarnings("unchecked")
        List<ReservaDetalleDto> cuerpo = (List<ReservaDetalleDto>) respuesta.getEntity();
        assertEquals(1, cuerpo.size());
        assertEquals("Laura Gómez", cuerpo.get(0).clienteNombre());
        assertEquals(EstadoReserva.RETENIDA, cuerpo.get(0).estado());
    }

    @Test
    void listarPorAdmin_sinReservas_devuelveListaVacia() {
        when(servicio.listarPorAdmin(3)).thenReturn(List.of());

        Response respuesta = recursos.listarPorAdmin(3);

        assertEquals(200, respuesta.getStatus());
        assertEquals(List.of(), respuesta.getEntity());
    }
}
