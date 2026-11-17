package org.parkfinder.calificaciones.infraestructura;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.parkfinder.calificaciones.aplicacion.CalificacionServicio;
import org.parkfinder.calificaciones.dominio.modelo.Calificacion;
import org.parkfinder.calificaciones.infraestructura.dto.CalificacionDto;
import org.parkfinder.calificaciones.infraestructura.dto.CalificacionRespuestaDto;
import org.parkfinder.calificaciones.infraestructura.dto.PermisoCalificarDto;
import org.parkfinder.calificaciones.infraestructura.dto.ResumenCalificacionesDto;

@Path("/calificaciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CalificacionRecursos {

    @Inject
    CalificacionServicio servicio;

    @POST
    @Operation(
            summary = "Calificar un parqueadero (HU-13)",
            description = "De 1 a 5 estrellas y un comentario opcional. Solo si el cliente completó una " +
                    "reserva en ese parqueadero; cada cliente califica una vez cada parqueadero"
    )
    @APIResponse(responseCode = "201", description = "Calificación registrada")
    @APIResponse(responseCode = "400", description = "Datos inválidos")
    @APIResponse(responseCode = "403", description = "El cliente no ha completado una reserva en el parqueadero")
    @APIResponse(responseCode = "409", description = "El cliente ya calificó este parqueadero")
    public Response calificar(@Valid CalificacionDto dto) {
        Calificacion creada = servicio.calificar(dto.clienteId(), dto.parqueaderoId(), dto.estrellas(),
                dto.comentario());
        return Response.status(Response.Status.CREATED).entity(CalificacionRespuestaDto.de(creada)).build();
    }

    @GET
    @Path("/parqueadero/{parqueaderoId}")
    @Operation(
            summary = "Calificaciones de un parqueadero (HU-13)",
            description = "Promedio, cantidad y lista de calificaciones con sus comentarios, las más " +
                    "recientes primero. Sin calificaciones: promedio null, total 0 y lista vacía"
    )
    @APIResponse(responseCode = "200", description = "Resumen de calificaciones")
    public Response resumen(@PathParam("parqueaderoId") Long parqueaderoId) {
        return Response.ok(ResumenCalificacionesDto.de(servicio.resumen(parqueaderoId))).build();
    }

    @GET
    @Path("/permiso")
    @Operation(
            summary = "¿Puede el cliente calificar este parqueadero? (HU-13)",
            description = "puede = completó una reserva en él y aún no lo calificó"
    )
    @APIResponse(responseCode = "200", description = "Permiso del cliente")
    public Response permiso(@QueryParam("clienteId") Long clienteId,
                            @QueryParam("parqueaderoId") Long parqueaderoId) {
        if (clienteId == null || parqueaderoId == null) {
            return Response.ok(new PermisoCalificarDto(false, false)).build();
        }
        return Response.ok(new PermisoCalificarDto(
                servicio.puedeCalificar(clienteId, parqueaderoId),
                servicio.yaCalifico(clienteId, parqueaderoId))).build();
    }
}
