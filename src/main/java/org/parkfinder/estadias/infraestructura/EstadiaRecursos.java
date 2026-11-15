package org.parkfinder.estadias.infraestructura;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.parkfinder.estadias.aplicacion.EstadiaServicio;
import org.parkfinder.estadias.aplicacion.EstadiaServicio.VehiculoIngreso;
import org.parkfinder.estadias.dominio.excepcion.EstadiaException;
import org.parkfinder.estadias.infraestructura.dto.EstadiaDto;
import org.parkfinder.estadias.infraestructura.dto.IngresoDto;
import org.parkfinder.estadias.infraestructura.dto.IngresoReservaDto;
import org.parkfinder.estadias.infraestructura.dto.LiquidacionDto;
import org.parkfinder.estadias.infraestructura.dto.SalidaDto;

@Path("/estadias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EstadiaRecursos {

    @Inject
    EstadiaServicio estadiaServicio;

    @POST
    @Path("/ingreso")
    @Operation(
            summary = "Ingreso directo de un vehículo",
            description = "El administrador digita la placa y el tipo de vehículo. El sistema guarda la fecha " +
                    "y hora de ingreso y descuenta un cupo de ese tipo."
    )
    @APIResponse(responseCode = "201", description = "Ingreso registrado")
    @APIResponse(responseCode = "400", description = "Placa inválida o datos incompletos")
    @APIResponse(responseCode = "403", description = "El parqueadero no es del administrador")
    @APIResponse(responseCode = "404", description = "El parqueadero no existe")
    @APIResponse(responseCode = "409", description = "La placa ya está adentro o no hay cupo de ese tipo")
    public Response ingreso(@Valid IngresoDto dto) {
        var estadia = estadiaServicio.ingresoDirecto(dto.parqueaderoId(), dto.adminId(), dto.placa(),
                dto.tipoVehiculo());
        return Response.status(Response.Status.CREATED).entity(EstadiaDto.de(estadia)).build();
    }

    @POST
    @Path("/ingreso-reserva")
    @Operation(
            summary = "Ingreso con código de reserva",
            description = "Valida el código de reserva y registra la placa de cada vehículo con la hora de " +
                    "ingreso. El cupo ya se había descontado al reservar."
    )
    @APIResponse(responseCode = "201", description = "Código validado e ingresos registrados")
    @APIResponse(responseCode = "400", description = "Placa inválida, repetida o más vehículos que los reservados")
    @APIResponse(responseCode = "403", description = "El parqueadero no es del administrador")
    @APIResponse(responseCode = "404", description = "El parqueadero o la reserva no existen")
    @APIResponse(responseCode = "409", description = "La reserva no está pagada, el código ya se usó o la placa ya está adentro")
    public Response ingresoConReserva(@Valid IngresoReservaDto dto) {
        var vehiculos = dto.vehiculos().stream()
                .map(v -> new VehiculoIngreso(v.placa(), v.tipoVehiculo()))
                .toList();
        var estadias = estadiaServicio.ingresoConReserva(dto.reservaId(), dto.parqueaderoId(), dto.adminId(),
                vehiculos);
        return Response.status(Response.Status.CREATED)
                .entity(estadias.stream().map(EstadiaDto::de).toList()).build();
    }

    @GET
    @Path("/cobro")
    @Operation(
            summary = "Consultar cuánto debe pagar un vehículo",
            description = "Calcula el valor con el tiempo transcurrido hasta ahora, sin liberar el cupo. " +
                    "Cada minuto vale tarifa hora / 60; la noche y cada bloque de 24 h tienen como tope su tarifa."
    )
    @APIResponse(responseCode = "200", description = "Valor a pagar hasta ahora")
    @APIResponse(responseCode = "403", description = "El parqueadero no es del administrador")
    @APIResponse(responseCode = "404", description = "No hay un vehículo con esa placa dentro del parqueadero")
    public Response consultarCobro(@QueryParam("parqueaderoId") Long parqueaderoId,
                                   @QueryParam("adminId") Integer adminId,
                                   @QueryParam("placa") String placa) {
        validarParametros(parqueaderoId, adminId);
        return Response.ok(LiquidacionDto.de(estadiaServicio.consultarCobro(parqueaderoId, adminId, placa))).build();
    }

    @POST
    @Path("/salida")
    @Operation(
            summary = "Liberar cupo y cobrar",
            description = "Registra la salida del vehículo con la hora actual, calcula el valor a pagar y " +
                    "devuelve el cupo de su tipo al parqueadero."
    )
    @APIResponse(responseCode = "200", description = "Salida registrada con el valor cobrado")
    @APIResponse(responseCode = "403", description = "El parqueadero no es del administrador")
    @APIResponse(responseCode = "404", description = "No hay un vehículo con esa placa dentro del parqueadero")
    @APIResponse(responseCode = "409", description = "El vehículo ya salió")
    public Response salida(@Valid SalidaDto dto) {
        return Response.ok(LiquidacionDto.de(
                estadiaServicio.registrarSalida(dto.parqueaderoId(), dto.adminId(), dto.placa()))).build();
    }

    @GET
    @Path("/parqueadero/{id}/activas")
    @Operation(
            summary = "Vehículos dentro del parqueadero",
            description = "Lista los vehículos que no han salido, con su hora de ingreso y lo que llevan acumulado."
    )
    @APIResponse(responseCode = "200", description = "Vehículos adentro (vacía si no hay)")
    @APIResponse(responseCode = "403", description = "El parqueadero no es del administrador")
    @APIResponse(responseCode = "404", description = "El parqueadero no existe")
    public Response listarActivas(@PathParam("id") Long parqueaderoId, @QueryParam("adminId") Integer adminId) {
        validarParametros(parqueaderoId, adminId);
        return Response.ok(estadiaServicio.listarActivas(parqueaderoId, adminId).stream()
                .map(LiquidacionDto::de).toList()).build();
    }

    private void validarParametros(Long parqueaderoId, Integer adminId) {
        if (parqueaderoId == null || adminId == null) {
            throw EstadiaException.invalida("El parqueadero y el id del administrador son requeridos");
        }
    }
}
