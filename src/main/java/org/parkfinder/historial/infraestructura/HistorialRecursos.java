package org.parkfinder.historial.infraestructura;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.parkfinder.historial.aplicacion.HistorialServicio;

@Path("/historial")
@Produces(MediaType.APPLICATION_JSON)
public class HistorialRecursos {

    @Inject
    HistorialServicio historialServicio;

    @GET
    @Path("/admin/{adminId}")
    @Operation(
            summary = "Historial de reservas e ingresos (HU-24)",
            description = "Reservas e ingresos de los parqueaderos del dueño en el día, la semana (lunes a " +
                    "domingo) o el mes que contiene la fecha. Los ingresos suman el pago aprobado de cada " +
                    "reserva y lo cobrado a cada vehículo al salir, con el desglose por día."
    )
    @APIResponse(responseCode = "200", description = "Historial del periodo (en ceros si no hubo movimiento)")
    @APIResponse(responseCode = "400", description = "Periodo o fecha inválidos")
    @APIResponse(responseCode = "403", description = "El parqueadero no es del administrador")
    @APIResponse(responseCode = "404", description = "El parqueadero no existe")
    public Response consultar(
            @PathParam("adminId") int adminId,
            @Parameter(description = "DIA, SEMANA o MES (por defecto DIA)") @QueryParam("periodo") String periodo,
            @Parameter(description = "Fecha AAAA-MM-DD dentro del periodo (por defecto hoy)") @QueryParam("fecha") String fecha,
            @Parameter(description = "Solo este parqueadero; sin él, todos los del dueño") @QueryParam("parqueaderoId") Long parqueaderoId) {
        return Response.ok(historialServicio.consultar(adminId, parqueaderoId, periodo, fecha)).build();
    }
}
