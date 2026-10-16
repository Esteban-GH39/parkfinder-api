package org.parkfinder.parqueaderos.infraestructura;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.parkfinder.common.infraestructure.ResponseApi;
import org.parkfinder.common.infraestructure.ResponseApiError;
import org.parkfinder.parqueaderos.dominio.excepcion.ParqueaderoNoEncontradoException;

import java.time.Instant;

/**
 * HU-19: traduce el parqueadero inexistente a un 404, con el mismo formato de
 * respuesta que usa {@code ValidationExceptionMapper}.
 */
@Provider
public class ParqueaderoNoEncontradoMapper
        implements ExceptionMapper<ParqueaderoNoEncontradoException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ParqueaderoNoEncontradoException exception) {
        var responseApi = new ResponseApi();
        responseApi.setStatus(Response.Status.NOT_FOUND.getStatusCode());
        responseApi.setTimestamp(Instant.now().toString());
        responseApi.setPath(uriInfo == null ? null : uriInfo.getPath());
        responseApi.setSucces(false);
        responseApi.setCodigo("Recurso no encontrado");
        responseApi.setMensaje(exception.getMessage());

        responseApi.setError(ResponseApiError.builder()
                .mensaje(exception.getMessage())
                .codigo("PARK-ERR-404")
                .build());

        return Response.status(Response.Status.NOT_FOUND)
                .entity(responseApi)
                .build();
    }
}
