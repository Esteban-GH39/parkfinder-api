package org.parkfinder.pagos.infraestructura;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.parkfinder.pagos.aplicacion.PagoServicio;
import org.parkfinder.pagos.infraestructura.dto.PagoConsultaDto;
import org.parkfinder.pagos.infraestructura.dto.PagoDto;

@Path("/pagos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PagoRecursos {

    @Inject
    PagoServicio pagoServicio;

    @POST
    @Operation(
            summary = "Pagar una reserva con la pasarela simulada (HU-07)",
            description = "Valida los datos del medio de pago y devuelve APROBADO o RECHAZADO. " +
                    "No se hace ninguna transacción real. Una tarjeta que termina en 0000 se rechaza; " +
                    "cualquier otro pago con los datos completos se aprueba."
    )
    @APIResponse(responseCode = "201", description = "Pago procesado (revisar el campo estado)")
    @APIResponse(responseCode = "400", description = "Faltan datos del medio de pago")
    @APIResponse(responseCode = "404", description = "La reserva no existe")
    @APIResponse(responseCode = "409", description = "La reserva venció o ya está pagada")
    public Response pagar(@Valid PagoDto pagoDto) {
        var pago = pagoServicio.pagar(pagoDto.reservaId(), pagoDto.medioPago(), pagoDto.datos());
        return Response.status(Response.Status.CREATED).entity(PagoConsultaDto.de(pago)).build();
    }
}
