package org.parkfinder.billetera.infraestructura;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
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
import org.parkfinder.billetera.aplicacion.BilleteraServicio;
import org.parkfinder.billetera.infraestructura.dto.MovimientoDto;
import org.parkfinder.billetera.infraestructura.dto.RecargaDto;
import org.parkfinder.billetera.infraestructura.dto.RecargaRespuestaDto;
import org.parkfinder.billetera.infraestructura.dto.SaldoDto;

@Path("/billetera")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BilleteraRecursos {

    @Inject
    BilleteraServicio servicio;

    @GET
    @Path("/{clienteId}")
    @Operation(
            summary = "Saldo de la billetera",
            description = "Saldo disponible del cliente en pesos. Es 0 si todavía no ha recargado."
    )
    @APIResponse(responseCode = "200", description = "Saldo del cliente")
    @APIResponse(responseCode = "404", description = "El cliente no existe")
    public Response saldo(@PathParam("clienteId") Long clienteId) {
        return Response.ok(new SaldoDto(clienteId, servicio.saldo(clienteId))).build();
    }

    @GET
    @Path("/{clienteId}/movimientos")
    @Operation(
            summary = "Movimientos de la billetera",
            description = "Recargas, pagos de reservas y reembolsos, el más reciente primero (entre 1 y 100, 20 por defecto)."
    )
    @APIResponse(responseCode = "200", description = "Movimientos (lista vacía si no tiene)")
    @APIResponse(responseCode = "404", description = "El cliente no existe")
    public Response movimientos(@PathParam("clienteId") Long clienteId,
                                @QueryParam("limite") @DefaultValue("20") int limite) {
        return Response.ok(servicio.movimientos(clienteId, limite).stream().map(MovimientoDto::de).toList()).build();
    }

    @POST
    @Path("/{clienteId}/recargas")
    @Operation(
            summary = "Recargar la billetera con la pasarela simulada",
            description = "Valida los datos del medio (tarjeta, PSE o Nequi) y devuelve APROBADO o RECHAZADO. " +
                    "No se hace ninguna transacción real. Una tarjeta que termina en 0000 se rechaza."
    )
    @APIResponse(responseCode = "201", description = "Recarga procesada (revisar el campo estado)")
    @APIResponse(responseCode = "400", description = "Monto fuera de límites o datos del medio de pago inválidos")
    @APIResponse(responseCode = "404", description = "El cliente no existe")
    public Response recargar(@PathParam("clienteId") Long clienteId, @Valid RecargaDto dto) {
        var recarga = servicio.recargar(clienteId, dto.monto(), dto.medioPago(), dto.datos());
        return Response.status(Response.Status.CREATED).entity(RecargaRespuestaDto.de(recarga)).build();
    }
}
