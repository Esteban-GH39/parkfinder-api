package org.parkfinder.clientes.infraestructura;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.parkfinder.clientes.aplicacion.ClienteServicio;
import org.parkfinder.clientes.dominio.modelo.Cliente;
import org.parkfinder.clientes.infraestructura.dto.ClienteDto;
import org.parkfinder.common.infraestructure.ResponseApi;
import org.parkfinder.common.infraestructure.ResponseApiError;

import java.time.Instant;

@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteRecursos {

    @Inject
    ClienteServicio clienteServicio;

    @Context
    UriInfo uriInfo;

    @POST
    @Operation(
            summary = "Registrar nuevo cliente",
            description = "Crea una cuenta de cliente con nombre, correo y contraseña, para acceder a búsqueda, reserva y pago"
    )
    @APIResponse(responseCode = "201", description = "Cliente registrado")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "409", description = "El correo ya está registrado")
    public Response registrar(@Valid ClienteDto clienteDto) {

        if (clienteServicio.correoYaRegistrado(clienteDto.correo())) {
            var responseApi = new ResponseApi();
            responseApi.setStatus(Response.Status.CONFLICT.getStatusCode());
            responseApi.setTimestamp(Instant.now().toString());
            responseApi.setPath(uriInfo.getPath());
            responseApi.setSucces(false);
            responseApi.setCodigo("CLIENTE-ERR-409");
            responseApi.setMensaje("El correo ya está registrado");
            responseApi.setError(ResponseApiError.builder()
                    .mensaje("El correo ya está registrado")
                    .codigo("CLIENTE-ERR-409")
                    .build());

            return Response.status(Response.Status.CONFLICT).entity(responseApi).build();
        }

        Cliente cliente = Cliente
                .builder()
                .nombre(clienteDto.nombre())
                .correo(clienteDto.correo())
                .contrasenaHash(io.quarkus.elytron.security.common.BcryptUtil.bcryptHash(clienteDto.contrasena()))
                .build();

        clienteServicio.registrarCliente(cliente);

        return Response.status(Response.Status.CREATED).build();
    }
}