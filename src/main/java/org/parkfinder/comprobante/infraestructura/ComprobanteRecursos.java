package org.parkfinder.comprobante.infraestructura;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.parkfinder.comprobante.aplicacion.ComprobanteServicio;
import org.parkfinder.comprobante.dominio.modelo.Comprobante;

@Path("/comprobante")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ComprobanteRecursos {

    @Inject
    ComprobanteServicio comprobanteServicio;

    @GET
    @Path("/{idComprobante}")
    @Operation(
            summary = "Mostrar comprobante",
            description = "Devuelve el comprobante por medio de su id"
    )
    @APIResponse(
            responseCode = "200",
            description = "Comprobante encontrado con exito"
    )
    @APIResponse(
            responseCode = "404",
            description = "El comprobante no se encuentra"
    )
    public Response comprobantePorId(@PathParam("idComprobante") Long id){
        Comprobante comprobante = comprobanteServicio.obtenerPorId(id);
        if (comprobante == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(comprobante).build();
    }
}
