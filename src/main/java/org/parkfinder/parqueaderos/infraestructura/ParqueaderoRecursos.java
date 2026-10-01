package org.parkfinder.parqueaderos.infraestructura;


import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.parkfinder.parqueaderos.aplicacion.ParqueaderoServicio;
import org.parkfinder.parqueaderos.dominio.modelo.HistorialCambio;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoActualizacion;
import org.parkfinder.parqueaderos.infraestructura.dto.HistorialCambioDto;
import org.parkfinder.parqueaderos.infraestructura.dto.ParqueaderoActualizarDto;
import org.parkfinder.parqueaderos.infraestructura.dto.ParqueaderoDto;

import java.util.List;

@Path("/parqueaderos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ParqueaderoRecursos {

    @Inject
    ParqueaderoServicio parqueaderoServicio;

    @POST
    @Operation(
            summary = "Crear nuevo parqueadero",
            description = "Registrar parqueadero con los datos, nombre, dirección, ubicación, capacidad total," +
                    " hora de inicio atención, hora de fin atención, representante"
    )
    @APIResponse(
            responseCode = "201",
            description = "Parqueadero registrado"
    )
    @APIResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos"
    )
    public Response crearPark(@Valid ParqueaderoDto parqueaderoDto){
        Parqueadero parqueadero = Parqueadero
                .builder()
                .nombre(parqueaderoDto.nombre())
                .direccion(parqueaderoDto.direccion())
                .zona(parqueaderoDto.zona())
                .capacidadTotal(parqueaderoDto.capacidadTotal())
                .tarifaHora(parqueaderoDto.tarifaHora())
                .tarifaDia(parqueaderoDto.tarifaDia())
                .tarifaNoche(parqueaderoDto.tarifaNoche())
                .horaInicio(parqueaderoDto.horaInicio())
                .horaFin(parqueaderoDto.horaFinal())
                .nombrePropietario(parqueaderoDto.nombrePropietario())
                .build();
        parqueaderoServicio.crearPark(parqueadero);
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Editar parqueadero",
            description = "Modifica la información del parqueadero y registra en el historial de " +
                    "trazabilidad cada campo que cambió. Los campos que no se envíen se dejan como están."
    )
    @APIResponse(
            responseCode = "200",
            description = "Parqueadero actualizado. Devuelve los cambios registrados."
    )
    @APIResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos"
    )
    @APIResponse(
            responseCode = "404",
            description = "El parqueadero no existe"
    )
    public Response actualizarPark(@PathParam("id") Long id,
                                   @Valid ParqueaderoActualizarDto parqueaderoDto){
        ParqueaderoActualizacion datos = ParqueaderoActualizacion
                .builder()
                .nombre(enBlancoComoNulo(parqueaderoDto.nombre()))
                .direccion(enBlancoComoNulo(parqueaderoDto.direccion()))
                .zona(enBlancoComoNulo(parqueaderoDto.zona()))
                .capacidadTotal(parqueaderoDto.capacidadTotal())
                .tarifaHora(parqueaderoDto.tarifaHora())
                .tarifaDia(parqueaderoDto.tarifaDia())
                .tarifaNoche(parqueaderoDto.tarifaNoche())
                .horaInicio(parqueaderoDto.horaInicio())
                .horaFin(parqueaderoDto.horaFinal())
                .nombrePropietario(enBlancoComoNulo(parqueaderoDto.nombrePropietario()))
                .build();

        List<HistorialCambio> cambios = parqueaderoServicio.actualizarPark(id, datos);
        return Response.ok(cambios.stream().map(HistorialCambioDto::de).toList()).build();
    }

    @GET
    @Path("/{id}/historial")
    @Operation(
            summary = "Historial de cambios del parqueadero",
            description = "Devuelve la trazabilidad de las ediciones: qué campo cambió, " +
                    "de qué valor a cuál y cuándo. El más reciente primero."
    )
    @APIResponse(
            responseCode = "200",
            description = "Historial del parqueadero"
    )
    @APIResponse(
            responseCode = "404",
            description = "El parqueadero no existe"
    )
    public Response historial(@PathParam("id") Long id){
        List<HistorialCambio> cambios = parqueaderoServicio.historial(id);
        return Response.ok(cambios.stream().map(HistorialCambioDto::de).toList()).build();
    }

    /**
     * Un texto vacío o en blanco se trata como "no cambiar", para que el
     * formulario no pueda dejar sin nombre a un parqueadero por descuido.
     */
    private String enBlancoComoNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

}
