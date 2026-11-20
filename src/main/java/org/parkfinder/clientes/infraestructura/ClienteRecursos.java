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
import org.parkfinder.clientes.infraestructura.dto.CambioContrasenaDto;
import org.parkfinder.clientes.infraestructura.dto.ClienteDto;
import org.parkfinder.clientes.infraestructura.dto.PerfilActualizarDto;
import org.parkfinder.clientes.infraestructura.dto.PerfilDto;
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
            description = "Crea una cuenta con nombre, correo y contraseña. Por defecto es de cliente (búsqueda, reserva y pago); " +
                    "con rol ADMIN es de dueño de parqueadero y además pide teléfono y cédula"
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

        boolean esAdmin = Cliente.ROL_ADMIN.equals(clienteDto.rol());
        if (esAdmin && (clienteDto.telefono() == null || clienteDto.cedula() == null)) {
            var responseApi = new ResponseApi();
            responseApi.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            responseApi.setTimestamp(Instant.now().toString());
            responseApi.setPath(uriInfo.getPath());
            responseApi.setSucces(false);
            responseApi.setCodigo("CLIENTE-ERR-400");
            responseApi.setMensaje("Un dueño debe indicar teléfono y cédula");
            responseApi.setError(ResponseApiError.builder()
                    .mensaje("Un dueño debe indicar teléfono y cédula")
                    .codigo("CLIENTE-ERR-400")
                    .build());

            return Response.status(Response.Status.BAD_REQUEST).entity(responseApi).build();
        }

        Cliente cliente = Cliente
                .builder()
                .nombre(clienteDto.nombre())
                .correo(clienteDto.correo())
                .contrasenaHash(io.quarkus.elytron.security.common.BcryptUtil.bcryptHash(clienteDto.contrasena()))
                .rol(esAdmin ? Cliente.ROL_ADMIN : Cliente.ROL_CLIENTE)
                .telefono(clienteDto.telefono())
                .cedula(clienteDto.cedula())
                .build();

        clienteServicio.registrarCliente(cliente);

        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Ver el perfil de una cuenta",
            description = "Devuelve nombre, correo y rol. Si la cuenta es ADMIN (dueño) también teléfono y cédula."
    )
    @APIResponse(responseCode = "200", description = "Perfil encontrado")
    @APIResponse(responseCode = "404", description = "La cuenta no existe")
    public Response obtenerPerfil(@PathParam("id") Long id) {
        return clienteServicio.obtenerPerfil(id)
                .map(c -> Response.ok(PerfilDto.de(c)).build())
                .orElseGet(this::cuentaNoEncontrada);
    }

    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Editar el perfil",
            description = "Cambia el nombre y, en cuentas ADMIN, el teléfono. El correo y el rol no se pueden cambiar."
    )
    @APIResponse(responseCode = "200", description = "Perfil actualizado")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "404", description = "La cuenta no existe")
    public Response actualizarPerfil(@PathParam("id") Long id, @Valid PerfilActualizarDto dto) {
        return clienteServicio.actualizarPerfil(id, dto.nombre(), dto.telefono())
                .map(c -> Response.ok(PerfilDto.de(c)).build())
                .orElseGet(this::cuentaNoEncontrada);
    }

    @PUT
    @Path("/{id}/contrasena")
    @Operation(
            summary = "Cambiar la contraseña",
            description = "Pide la contraseña actual y la nueva (mínimo 8 caracteres, con letra y número)."
    )
    @APIResponse(responseCode = "204", description = "Contraseña cambiada")
    @APIResponse(responseCode = "400", description = "La contraseña actual no es correcta o la nueva no cumple el formato")
    @APIResponse(responseCode = "404", description = "La cuenta no existe")
    public Response cambiarContrasena(@PathParam("id") Long id, @Valid CambioContrasenaDto dto) {
        return switch (clienteServicio.cambiarContrasena(id, dto.contrasenaActual(), dto.contrasenaNueva())) {
            case CAMBIADA -> Response.noContent().build();
            case NO_EXISTE -> cuentaNoEncontrada();
            case CONTRASENA_INCORRECTA -> error(Response.Status.BAD_REQUEST, "CLIENTE-ERR-400",
                    "La contraseña actual no es correcta");
        };
    }

    private Response cuentaNoEncontrada() {
        return error(Response.Status.NOT_FOUND, "CLIENTE-ERR-404", "La cuenta no existe");
    }

    private Response error(Response.Status estado, String codigo, String mensaje) {
        var responseApi = new ResponseApi();
        responseApi.setStatus(estado.getStatusCode());
        responseApi.setTimestamp(Instant.now().toString());
        responseApi.setPath(uriInfo.getPath());
        responseApi.setSucces(false);
        responseApi.setCodigo(codigo);
        responseApi.setMensaje(mensaje);
        responseApi.setError(ResponseApiError.builder().mensaje(mensaje).codigo(codigo).build());
        return Response.status(estado).entity(responseApi).build();
    }
}
