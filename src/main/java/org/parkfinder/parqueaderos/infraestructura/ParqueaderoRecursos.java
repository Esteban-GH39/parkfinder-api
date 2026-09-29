package org.parkfinder.parqueaderos.infraestructura;


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
import org.parkfinder.parqueaderos.aplicacion.ParqueaderoServicio;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.infraestructura.dto.ParqueaderoDto;

@Path("/parqueaderos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ParqueaderoRecursos {

    @Inject
    ParqueaderoServicio parqueaderoServicio;

    @POST
    @Operation(
            summary = "Crear nuevo parqueadero",
            description = "Registrar parqueadero con los datos, nombre, dirección, ubicación, capacidad total," +
                    " hora de inicio atención, hora de fin atención, representante"
    )
    @APIResponse(
            responseCode = "201",
            description = "Parqueadero registrado"
    )
    @APIResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos"
    )
    public Response crearPark(@Valid ParqueaderoDto parqueaderoDto){
        Parqueadero parqueadero = Parqueadero
                .builder()
                .nombre(parqueaderoDto.nombre())
                .direccion(parqueaderoDto.direccion())
                .zona(parqueaderoDto.zona())
                .capacidadTotal(parqueaderoDto.capacidadTotal())
                .tarifaHora(parqueaderoDto.tarifaHora())
                .tarifaDia(parqueaderoDto.tarifaDia())
                .tarifaNoche(parqueaderoDto.tarifaNoche())
                .horaInicio(parqueaderoDto.horaInicio())
                .horaFin(parqueaderoDto.horaFinal())
                .nombrePropietario(parqueaderoDto.nombrePropietario())
                .build();
        parqueaderoServicio.crearPark(parqueadero);
        return Response.status(Response.Status.CREATED).build();
    }


}
