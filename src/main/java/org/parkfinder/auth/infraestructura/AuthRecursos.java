package org.parkfinder.auth.infraestructura;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.parkfinder.auth.infraestructura.dto.LoginDto;
import org.parkfinder.auth.infraestructura.dto.SesionDto;
import org.parkfinder.clientes.aplicacion.ClienteServicio;
import org.parkfinder.common.infraestructure.ResponseApi;
import org.parkfinder.common.infraestructure.ResponseApiError;

import java.time.Instant;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthRecursos {

    @Inject
    ClienteServicio clienteServicio;

    @Context
    UriInfo uriInfo;

    @POST
    @Path("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Valida correo y contraseña. Si son correctos devuelve id, nombre, correo y rol (CLIENTE o ADMIN); si es ADMIN, también adminId, el dueño de sus parqueaderos"
    )
    @APIResponse(responseCode = "200", description = "Sesión iniciada")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "401", description = "Credenciales incorrectas")
    public Response login(@Valid LoginDto loginDto) {
        return clienteServicio.autenticar(loginDto.correo(), loginDto.contrasena())
                .map(c -> Response.ok(new SesionDto(c.id, c.nombre, c.correo, c.rol, c.adminId)).build())
                .orElseGet(() -> {
                    var responseApi = new ResponseApi();
                    responseApi.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
                    responseApi.setTimestamp(Instant.now().toString());
                    responseApi.setPath(uriInfo.getPath());
                    responseApi.setSucces(false);
                    responseApi.setCodigo("AUTH-ERR-401");
                    responseApi.setMensaje("Correo o contraseña incorrectos");
                    responseApi.setError(ResponseApiError.builder()
                            .mensaje("Correo o contraseña incorrectos")
                            .codigo("AUTH-ERR-401")
                            .build());
                    return Response.status(Response.Status.UNAUTHORIZED).entity(responseApi).build();
                });
    }
}