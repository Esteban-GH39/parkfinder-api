package com.parkfinder.users;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Traduce HU-01/HU-16 "email ya registrado" a un 409 Conflict con mensaje claro. */
@Provider
public class EmailYaRegistradoExceptionMapper implements ExceptionMapper<EmailYaRegistradoException> {

    @Override
    public Response toResponse(EmailYaRegistradoException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    public record ErrorResponse(String mensaje) {
    }
}
