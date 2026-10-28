package org.parkfinder.reservas.infraestructura;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.parkfinder.common.infraestructure.ResponseApi;
import org.parkfinder.common.infraestructure.ResponseApiError;
import org.parkfinder.reservas.dominio.excepcion.ReservaException;

import java.time.Instant;

/** HU-06: traduce los errores de la reserva a su código HTTP, con el formato de respuesta común. */
@Provider
public class ReservaExceptionMapper implements ExceptionMapper<ReservaException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ReservaException exception) {
        var responseApi = new ResponseApi();
        responseApi.setStatus(exception.getStatus());
        responseApi.setTimestamp(Instant.now().toString());
        responseApi.setPath(uriInfo == null ? null : uriInfo.getPath());
        responseApi.setSucces(false);
        responseApi.setCodigo(exception.getCodigo());
        responseApi.setMensaje(exception.getMessage());
        responseApi.setError(ResponseApiError.builder()
                .mensaje(exception.getMessage())
                .codigo(exception.getCodigo())
                .build());

        return Response.status(exception.getStatus()).entity(responseApi).build();
    }
}
