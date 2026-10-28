package org.parkfinder.reservas.infraestructura;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkfinder.reservas.aplicacion.ReservaServicio;
import org.parkfinder.reservas.dominio.excepcion.ReservaNoEncontradaException;
import org.parkfinder.reservas.dominio.excepcion.SinCupoException;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.infraestructura.dto.ReservaConsultaDto;
import org.parkfinder.reservas.infraestructura.dto.ReservaDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * HU-06: pruebas del recurso REST sin levantar Quarkus ni tocar la base de datos.
 * El servicio se simula, así que solo se verifica el contrato HTTP.
 */
class ReservaRecursosHu06Test {

    private ReservaServicio servicio;
    private ReservaRecursos recursos;

    private final LocalDateTime llegada = LocalDateTime.of(2026, 11, 2, 10, 0);

    @BeforeEach
    void preparar() {
        servicio = mock(ReservaServicio.class);
        recursos = new ReservaRecursos();
        recursos.reservaServicio = servicio;
    }

    private Reserva reservaDePrueba() {
        return Reserva.builder()
                .id(5L)
                .clienteId(1L)
                .parqueaderoId(7L)
                .fechaHoraLlegada(llegada)
                .estado(EstadoReserva.RETENIDA)
                .creadaEn(llegada.minusDays(1))
                .expiraEn(llegada.minusDays(1).plusMinutes(10))
                .build();
    }

    @Test
    void reservar_devuelve201ConLaReservaRetenida() {
        when(servicio.reservar(1L, 7L, llegada)).thenReturn(reservaDePrueba());

        Response respuesta = recursos.reservar(new ReservaDto(1L, 7L, llegada));

        assertEquals(201, respuesta.getStatus());
        ReservaConsultaDto dto = (ReservaConsultaDto) respuesta.getEntity();
        assertEquals(5L, dto.id());
        assertEquals(EstadoReserva.RETENIDA, dto.estado());
        assertEquals(llegada.minusDays(1).plusMinutes(10), dto.expiraEn());
    }

    @Test
    void reservar_sinCupoPropagaElErrorParaQueElMapperResponda409() {
        when(servicio.reservar(1L, 7L, llegada)).thenThrow(new SinCupoException(7L));

        SinCupoException error = assertThrows(SinCupoException.class,
                () -> recursos.reservar(new ReservaDto(1L, 7L, llegada)));

        assertEquals(409, error.getStatus());
    }

    @Test
    void obtener_devuelve200ConLaReserva() {
        when(servicio.obtenerPorId(5L)).thenReturn(reservaDePrueba());

        Response respuesta = recursos.obtener(5L);

        assertEquals(200, respuesta.getStatus());
        assertEquals(7L, ((ReservaConsultaDto) respuesta.getEntity()).parqueaderoId());
    }

    @Test
    void obtener_reservaInexistentePropagaNoEncontrada() {
        when(servicio.obtenerPorId(99L)).thenThrow(new ReservaNoEncontradaException(99L));

        ReservaNoEncontradaException error = assertThrows(ReservaNoEncontradaException.class,
                () -> recursos.obtener(99L));

        assertEquals(404, error.getStatus());
    }
}
