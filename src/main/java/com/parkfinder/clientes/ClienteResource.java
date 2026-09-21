package com.parkfinder.clientes;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

    @GET
    public java.util.List<Cliente> listar() {
        return Cliente.listAll();
    }

    @POST
    @Path("/registro")
    @Transactional
    public Response registrar(ClienteRegistroRequest request) {
        // Validaciones según criterios de aceptación de HU-01
        if (request.nombre == null || request.nombre.isBlank()
                || request.correo == null || request.correo.isBlank()
                || request.contrasena == null) {
            return Response.status(400)
                    .entity("{\"error\":\"nombre, correo y contraseña son obligatorios\"}")
                    .build();
        }

        if (!request.contrasena.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            return Response.status(400)
                    .entity("{\"error\":\"la contraseña debe tener mínimo 8 caracteres, con al menos una letra y un número\"}")
                    .build();
        }

        if (Cliente.find("correo", request.correo).firstResultOptional().isPresent()) {
            return Response.status(409)
                    .entity("{\"error\":\"el correo ya está registrado\"}")
                    .build();
        }

        Cliente cliente = new Cliente();
        cliente.nombre = request.nombre;
        cliente.correo = request.correo;
        cliente.contrasenaHash = BcryptUtil.bcryptHash(request.contrasena);
        cliente.persist();

        return Response.status(201)
                .entity("{\"id\":" + cliente.id + ",\"nombre\":\"" + cliente.nombre + "\"}")
                .build();
    }
}