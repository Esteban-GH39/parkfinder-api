package org.parkfinder.parqueaderos.infraestructura;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.parkfinder.parqueaderos.aplicacion.ParqueaderoServicio;
import org.parkfinder.parqueaderos.dominio.excepcion.ParqueaderoNoEncontradoException;
import org.parkfinder.parqueaderos.dominio.modelo.HistorialCambio;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoActualizacion;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoConsulta;
import org.parkfinder.parqueaderos.infraestructura.dto.HistorialCambioDto;
import org.parkfinder.parqueaderos.infraestructura.dto.ParqueaderoActualizarDto;
import org.parkfinder.parqueaderos.infraestructura.dto.ParqueaderoConsultaDto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-19: pruebas del recurso REST sin levantar Quarkus ni tocar la base de datos.
 * El servicio se simula, así que solo se verifica el contrato HTTP y la
 * conversión de datos del recurso.
 */
class ParqueaderoRecursosHu19Test {

    private ParqueaderoServicio servicio;
    private ParqueaderoRecursos recursos;

    @BeforeEach
    void preparar() {
        servicio = mock(ParqueaderoServicio.class);
        recursos = new ParqueaderoRecursos();
        recursos.parqueaderoServicio = servicio;
    }

    private ParqueaderoConsulta parqueaderoDePrueba() {
        return ParqueaderoConsulta.builder()
                .id(7L)
                .nombre("Parqueadero Centro")
                .direccion("Calle 53 #10-20")
                .zona("Chapinero")
                .capacidadTotal(50)
                .tarifaHora(3000.0)
                .tarifaDia(20000.0)
                .tarifaNoche(15000.0)
                .horaInicio(LocalTime.of(7, 0))
                .horaFin(LocalTime.of(20, 0))
                .nombrePropietario("Laura Gómez")
                .build();
    }

    @Test
    void obtener_devuelve200ConLosDatosDelParqueadero() {
        when(servicio.obtenerPorId(7L)).thenReturn(parqueaderoDePrueba());

        Response respuesta = recursos.obtener(7L);

        assertEquals(200, respuesta.getStatus());
        ParqueaderoConsultaDto dto = (ParqueaderoConsultaDto) respuesta.getEntity();
        assertEquals(7L, dto.id());
        assertEquals("Chapinero", dto.zona());
        assertEquals(50, dto.capacidadTotal());
        assertEquals(3000.0, dto.tarifaHora());
        assertEquals(20000.0, dto.tarifaDia());
        assertEquals(15000.0, dto.tarifaNoche());
        assertEquals(LocalTime.of(20, 0), dto.horaFin());
    }

    @Test
    void obtener_propagaNoEncontradoParaQueElMapperResponda404() {
        when(servicio.obtenerPorId(99L)).thenThrow(new ParqueaderoNoEncontradoException(99L));

        assertThrows(ParqueaderoNoEncontradoException.class, () -> recursos.obtener(99L));
    }

    @Test
    void listar_devuelve200ConTodosLosParqueaderos() {
        when(servicio.listar()).thenReturn(List.of(parqueaderoDePrueba()));

        Response respuesta = recursos.listar();

        assertEquals(200, respuesta.getStatus());
        @SuppressWarnings("unchecked")
        List<ParqueaderoConsultaDto> lista = (List<ParqueaderoConsultaDto>) respuesta.getEntity();
        assertEquals(1, lista.size());
        assertEquals("Parqueadero Centro", lista.get(0).nombre());
    }

    @Test
    void listar_sinParqueaderosDevuelveListaVacia() {
        when(servicio.listar()).thenReturn(List.of());

        Response respuesta = recursos.listar();

        assertEquals(200, respuesta.getStatus());
        assertEquals(List.of(), respuesta.getEntity());
    }

    @Test
    void actualizar_convierteTextosEnBlancoANuloYTraduceHoraFinal() {
        when(servicio.actualizarPark(eq(7L), any())).thenReturn(List.of());
        ParqueaderoActualizarDto dto = new ParqueaderoActualizarDto(
                "  ", "  Calle 1  ", null, 80, 3500.0, 25000.0, 18000.0,
                LocalTime.of(8, 0), LocalTime.of(21, 0), "");

        recursos.actualizarPark(7L, dto);

        ArgumentCaptor<ParqueaderoActualizacion> captor =
                ArgumentCaptor.forClass(ParqueaderoActualizacion.class);
        verify(servicio).actualizarPark(eq(7L), captor.capture());
        ParqueaderoActualizacion datos = captor.getValue();
        assertNull(datos.nombre, "un nombre en blanco significa 'no cambiar'");
        assertEquals("Calle 1", datos.direccion);
        assertNull(datos.zona);
        assertEquals(80, datos.capacidadTotal);
        assertEquals(3500.0, datos.tarifaHora);
        assertEquals(25000.0, datos.tarifaDia);
        assertEquals(18000.0, datos.tarifaNoche);
        assertEquals(LocalTime.of(8, 0), datos.horaInicio);
        assertEquals(LocalTime.of(21, 0), datos.horaFin, "horaFinal del DTO es horaFin del dominio");
        assertNull(datos.nombrePropietario);
    }

    @Test
    void actualizar_devuelve200ConLosCambiosRegistrados() {
        HistorialCambio cambio = HistorialCambio.builder()
                .parqueaderoId(7L)
                .campo("zona")
                .valorAnterior("Chapinero")
                .valorNuevo("Usaquen")
                .fecha(LocalDateTime.of(2026, 9, 30, 20, 0))
                .build();
        when(servicio.actualizarPark(eq(7L), any())).thenReturn(List.of(cambio));

        Response respuesta = recursos.actualizarPark(7L,
                new ParqueaderoActualizarDto(null, null, "Usaquen", null, null, null, null,
                        null, null, null));

        assertEquals(200, respuesta.getStatus());
        @SuppressWarnings("unchecked")
        List<HistorialCambioDto> cambios = (List<HistorialCambioDto>) respuesta.getEntity();
        assertEquals(1, cambios.size());
        assertEquals("zona", cambios.get(0).campo());
        assertEquals("Chapinero", cambios.get(0).valorAnterior());
        assertEquals("Usaquen", cambios.get(0).valorNuevo());
    }

    @Test
    void actualizar_sinCambiosReales_devuelve200ConListaVacia() {
        when(servicio.actualizarPark(eq(7L), any())).thenReturn(List.of());

        Response respuesta = recursos.actualizarPark(7L,
                new ParqueaderoActualizarDto(null, null, null, null, null, null, null,
                        null, null, null));

        assertEquals(200, respuesta.getStatus());
        assertEquals(List.of(), respuesta.getEntity());
    }

    @Test
    void actualizar_propagaNoEncontrado() {
        when(servicio.actualizarPark(eq(99L), any())).thenThrow(new ParqueaderoNoEncontradoException(99L));

        assertThrows(ParqueaderoNoEncontradoException.class, () ->
                recursos.actualizarPark(99L, new ParqueaderoActualizarDto(
                        null, null, null, null, null, null, null, null, null, null)));
    }

    @Test
    void historial_devuelve200ConLosRenglones() {
        HistorialCambio cambio = HistorialCambio.builder()
                .parqueaderoId(7L)
                .campo("tarifaDia")
                .valorAnterior("20000.0")
                .valorNuevo("25000.0")
                .fecha(LocalDateTime.of(2026, 9, 30, 20, 0))
                .build();
        when(servicio.historial(7L)).thenReturn(List.of(cambio));

        Response respuesta = recursos.historial(7L);

        assertEquals(200, respuesta.getStatus());
        @SuppressWarnings("unchecked")
        List<HistorialCambioDto> cambios = (List<HistorialCambioDto>) respuesta.getEntity();
        assertEquals("tarifaDia", cambios.get(0).campo());
    }
}
