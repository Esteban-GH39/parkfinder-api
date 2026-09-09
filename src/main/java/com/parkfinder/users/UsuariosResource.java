package com.parkfinder.users;

import com.parkfinder.users.dto.RegistroClienteRequest;
import com.parkfinder.users.dto.UsuarioResponse;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** HU-01: registro de cuenta de cliente. */
@Path("/usuarios")
public class UsuariosResource {

    @Inject
    UsuarioService usuarioService;

    @POST
    @Path("/clientes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarCliente(@Valid RegistroClienteRequest request) {
        Usuario usuario = usuarioService.registrarCliente(request);
        return Response.status(Response.Status.CREATED)
                .entity(UsuarioResponse.de(usuario))
                .build();
    }
}
