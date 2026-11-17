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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Historial de reservas del cliente: recurso REST sin levantar Quarkus ni tocar la base de datos. */
class ReservaRecursosHistorialClienteTest {

    private ReservaServicio servicio;
    private ReservaRecursos recursos;

    @BeforeEach
    void preparar() {
        servicio = mock(ReservaServicio.class);
        recursos = new ReservaRecursos();
        recursos.reservaServicio = servicio;
    }

    @Test
    void listarHistorial_devuelveReservasDeTodosLosEstados() {
        LocalDateTime t = LocalDateTime.of(2026, 11, 2, 10, 0);
        when(servicio.listarHistorialPorCliente(5L)).thenReturn(List.of(
                ReservaDetalle.builder().id(3L).parqueaderoNombre("Centro").estado(EstadoReserva.CONFIRMADA)
                        .codigoReserva(77L).fechaHoraLlegada(t).creadaEn(t).build(),
                ReservaDetalle.builder().id(2L).parqueaderoNombre("Norte").estado(EstadoReserva.EXPIRADA)
                        .fechaHoraLlegada(t).creadaEn(t.minusDays(1)).build()));

        Response r = recursos.listarHistorial(5L);

        assertEquals(200, r.getStatus());
        @SuppressWarnings("unchecked")
        List<ReservaDetalleDto> cuerpo = (List<ReservaDetalleDto>) r.getEntity();
        assertEquals(2, cuerpo.size());
        assertEquals(EstadoReserva.CONFIRMADA, cuerpo.get(0).estado());
        assertNotNull(cuerpo.get(0).codigoReserva());
        assertEquals(EstadoReserva.EXPIRADA, cuerpo.get(1).estado());
    }

    @Test
    void listarHistorial_sinReservas_devuelveListaVacia() {
        when(servicio.listarHistorialPorCliente(9L)).thenReturn(List.of());

        Response r = recursos.listarHistorial(9L);

        assertEquals(200, r.getStatus());
        assertTrue(((List<?>) r.getEntity()).isEmpty());
    }
}
