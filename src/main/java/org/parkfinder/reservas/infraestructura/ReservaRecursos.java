package org.parkfinder.reservas.infraestructura;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.parkfinder.reservas.aplicacion.ReservaServicio;
import org.parkfinder.reservas.dominio.excepcion.ReservaInvalidaException;
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
                reservaDto.clienteId(), reservaDto.parqueaderoId(), reservaDto.fechaHoraLlegada(),
                reservaDto.cantidadCarros(), reservaDto.cantidadMotos());
        return Response.status(Response.Status.CREATED).entity(ReservaConsultaDto.de(reserva, reservaServicio.costoReserva())).build();
    }

    @GET
    @Path("/cliente/{clienteId}")
    @Operation(
            summary = "Historial de reservas de un cliente",
            description = "Lista todas las reservas del cliente (pendientes de pago, confirmadas, usadas y vencidas), " +
                    "la más reciente primero. Incluye el código de reserva cuando ya está pagada."
    )
    @APIResponse(responseCode = "200", description = "Historial del cliente (vacío si no tiene reservas)")
    public Response listarHistorial(@PathParam("clienteId") Long clienteId) {
        return Response.ok(reservaServicio.listarHistorialPorCliente(clienteId).stream()
                .map(r -> ReservaDetalleDto.de(r, reservaServicio.costoReserva())).toList()).build();
    }

    @GET
    @Path("/cliente/{clienteId}/pendientes")
    @Operation(
            summary = "Reservas pendientes de pago de un cliente (HU-07)",
            description = "Lista las reservas retenidas que todavía no vencen, para retomar el pago. " +
                    "Incluye la tarifa del parqueadero."
    )
    @APIResponse(responseCode = "200", description = "Reservas pendientes (vacía si no tiene)")
    public Response listarPendientes(@PathParam("clienteId") Long clienteId) {
        return Response.ok(reservaServicio.listarPendientesPorCliente(clienteId).stream()
                .map(r -> ReservaDetalleDto.de(r, reservaServicio.costoReserva())).toList()).build();
    }

    @GET
    @Path("/cliente/{clienteId}/confirmadas")
    @Operation(
            summary = "Reservas confirmadas de un cliente",
            description = "Lista las reservas ya pagadas que todavía no se han usado, con su código de reserva."
    )
    @APIResponse(responseCode = "200", description = "Reservas confirmadas (vacía si no tiene)")
    public Response listarConfirmadas(@PathParam("clienteId") Long clienteId) {
        return Response.ok(reservaServicio.listarConfirmadasPorCliente(clienteId).stream()
                .map(r -> ReservaDetalleDto.de(r, reservaServicio.costoReserva())).toList()).build();
    }

    @POST
    @Path("/{id}/validar")
    @Operation(
            summary = "Validar el código de reserva",
            description = "El dueño confirma que el cliente llegó: queda anotada la hora y el código no se puede usar de nuevo."
    )
    @APIResponse(responseCode = "200", description = "Código validado")
    @APIResponse(responseCode = "404", description = "La reserva no existe")
    @APIResponse(responseCode = "409", description = "La reserva no está confirmada o ya se usó")
    public Response validar(@PathParam("id") Long id) {
        return Response.ok(ReservaConsultaDto.de(reservaServicio.validar(id), reservaServicio.costoReserva())).build();
    }

    @POST
    @Path("/{id}/cancelar")
    @Operation(
            summary = "Cancelar una reserva confirmada (HU-10)",
            description = "El cliente cancela su reserva confirmada y el cupo vuelve al parqueadero. Solo se " +
                    "puede hasta unos minutos antes de la hora de llegada (parkfinder.reserva.cancelacion-minutos-antes)."
    )
    @APIResponse(responseCode = "200", description = "Reserva cancelada y cupo liberado")
    @APIResponse(responseCode = "400", description = "La reserva no es de ese cliente")
    @APIResponse(responseCode = "404", description = "La reserva no existe")
    @APIResponse(responseCode = "409", description = "No está confirmada, ya se usó o pasó el tiempo límite")
    public Response cancelar(@PathParam("id") Long id, @QueryParam("clienteId") Long clienteId) {
        if (clienteId == null) {
            throw new ReservaInvalidaException("Falta el clienteId");
        }
        return Response.ok(ReservaConsultaDto.de(reservaServicio.cancelar(id, clienteId), reservaServicio.costoReserva())).build();
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
