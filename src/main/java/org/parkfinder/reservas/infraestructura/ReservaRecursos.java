package org.parkfinder.reservas.infraestructura;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.parkfinder.reservas.aplicacion.ReservaServicio;
import org.parkfinder.reservas.infraestructura.dto.ReservaConsultaDto;
import org.parkfinder.reservas.infraestructura.dto.ReservaDetalleDto;
import org.parkfinder.reservas.infraestructura.dto.ReservaDto;

@Path("/reservas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReservaRecursos {

    @Inject
    ReservaServicio reservaServicio;

    @POST
    @Operation(
            summary = "Reservar un cupo (HU-06)",
            description = "Valida la disponibilidad y retiene un cupo del parqueadero para la fecha y hora " +
                    "de llegada. La retención dura unos minutos mientras el cliente completa el pago."
    )
    @APIResponse(responseCode = "201", description = "Cupo retenido")
    @APIResponse(responseCode = "400", description = "Datos inválidos, fecha pasada o fuera de horario")
    @APIResponse(responseCode = "404", description = "El parqueadero no existe")
    @APIResponse(responseCode = "409", description = "No hay cupos disponibles; no se genera retención")
    public Response reservar(@Valid ReservaDto reservaDto) {
        var reserva = reservaServicio.reservar(
                reservaDto.clienteId(), reservaDto.parqueaderoId(), reservaDto.fechaHoraLlegada());
        return Response.status(Response.Status.CREATED).entity(ReservaConsultaDto.de(reserva)).build();
    }

    @GET
    @Path("/admin/{adminId}")
    @Operation(
            summary = "Reservas de mis parqueaderos",
            description = "Lista las reservas de los parqueaderos de un dueño, la más reciente primero, " +
                    "con el cliente que reservó y el estado. Las retenciones vencidas se marcan EXPIRADA."
    )
    @APIResponse(responseCode = "200", description = "Reservas del dueño (vacía si no tiene)")
    public Response listarPorAdmin(@PathParam("adminId") int adminId) {
        return Response.ok(reservaServicio.listarPorAdmin(adminId).stream()
                .map(ReservaDetalleDto::de).toList()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Consultar una reserva (HU-06)",
            description = "Devuelve el estado de la reserva y cuándo vence la retención."
    )
    @APIResponse(responseCode = "200", description = "Reserva encontrada")
    @APIResponse(responseCode = "404", description = "La reserva no existe")
    public Response obtener(@PathParam("id") Long id) {
        return Response.ok(ReservaConsultaDto.de(reservaServicio.obtenerPorId(id))).build();
    }
}
