package org.parkfinder.parqueaderos.infraestructura;


import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.parkfinder.parqueaderos.aplicacion.ParqueaderoServicio;
import org.parkfinder.parqueaderos.dominio.modelo.HistorialCambio;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoActualizacion;
import org.parkfinder.parqueaderos.infraestructura.dto.*;

import java.util.List;

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
                .fk_parqueadero_admin(parqueaderoDto.fk_parqueadero_admin())
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
                .cuposDisponibles(parqueaderoDto.capacidadTotal())
                .build();
        parqueaderoServicio.crearPark(parqueadero);
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Editar parqueadero",
            description = "Modifica la información del parqueadero y registra en el historial de " +
                    "trazabilidad cada campo que cambió. Los campos que no se envíen se dejan como están."
    )
    @APIResponse(
            responseCode = "200",
            description = "Parqueadero actualizado. Devuelve los cambios registrados."
    )
    @APIResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos"
    )
    @APIResponse(
            responseCode = "404",
            description = "El parqueadero no existe"
    )
    public Response actualizarPark(@PathParam("id") Long id,
                                   @Valid ParqueaderoActualizarDto parqueaderoDto){
        ParqueaderoActualizacion datos = ParqueaderoActualizacion
                .builder()
                .nombre(enBlancoComoNulo(parqueaderoDto.nombre()))
                .direccion(enBlancoComoNulo(parqueaderoDto.direccion()))
                .zona(enBlancoComoNulo(parqueaderoDto.zona()))
                .capacidadTotal(parqueaderoDto.capacidadTotal())
                .tarifaHora(parqueaderoDto.tarifaHora())
                .tarifaDia(parqueaderoDto.tarifaDia())
                .tarifaNoche(parqueaderoDto.tarifaNoche())
                .horaInicio(parqueaderoDto.horaInicio())
                .horaFin(parqueaderoDto.horaFinal())
                .nombrePropietario(enBlancoComoNulo(parqueaderoDto.nombrePropietario()))
                .build();

        List<HistorialCambio> cambios = parqueaderoServicio.actualizarPark(id, datos);
        return Response.ok(cambios.stream().map(HistorialCambioDto::de).toList()).build();
    }

    @GET
    @Operation(
            summary = "Buscar parqueaderos por zona (HU-03)",
            description = "Con el parámetro opcional 'zona' devuelve los parqueaderos cuya zona " +
                    "contiene el texto (sin distinguir mayúsculas). Sin zona devuelve todos."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de parqueaderos (vacía si no hay coincidencias)"
    )
    public Response buscar(@QueryParam("zona") String zona){
        return Response.ok(parqueaderoServicio.buscar(zona).stream()
                .map(ParqueaderoConsultaDto::de).toList()).build();
    }

    @GET
    @Path("/zonas")
    @Operation(
            summary = "Listar zonas existentes (HU-03)",
            description = "Devuelve las zonas distintas registradas, para que el cliente elija una."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de zonas (vacía si no hay parqueaderos)"
    )
    public Response zonas(){
        return Response.ok(parqueaderoServicio.listarZonas()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Consultar un parqueadero",
            description = "Devuelve los datos actuales de un parqueadero, para precargar " +
                    "la pantalla de edición."
    )
    @APIResponse(
            responseCode = "200",
            description = "Parqueadero encontrado"
    )
    @APIResponse(
            responseCode = "404",
            description = "El parqueadero no existe"
    )
    public Response obtener(@PathParam("id") Long id){
        return Response.ok(ParqueaderoConsultaDto.de(parqueaderoServicio.obtenerPorId(id))).build();
    }

    @GET
    @Path("/{id}/historial")
    @Operation(
            summary = "Historial de cambios del parqueadero",
            description = "Devuelve la trazabilidad de las ediciones: qué campo cambió, " +
                    "de qué valor a cuál y cuándo. El más reciente primero."
    )
    @APIResponse(
            responseCode = "200",
            description = "Historial del parqueadero"
    )
    @APIResponse(
            responseCode = "404",
            description = "El parqueadero no existe"
    )
    public Response historial(@PathParam("id") Long id){
        List<HistorialCambio> cambios = parqueaderoServicio.historial(id);
        return Response.ok(cambios.stream().map(HistorialCambioDto::de).toList()).build();
    }

    /**
     * Un texto vacío o en blanco se trata como "no cambiar", para que el
     * formulario no pueda dejar sin nombre a un parqueadero por descuido.
     */
    private String enBlancoComoNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }



    //ENDPOINT PARA LISTAR PARQUEADERO POR ID DE ADMIN (HU-21)
    @GET
    @Path("/admin/{id}")
    @Operation(
            summary = "Listar parqueaderos de un administrador",
            description = "Devuelve la lista de parqueaderos registrados por el ID del administrador especificado"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista obtenida con éxito"
    )
    public Response listarPorAdmin(@PathParam("id")Integer id){
        List<Parqueadero> parqueaderos = parqueaderoServicio.listarPorAdmin(id);
        return Response.ok(parqueaderos).build();
    }

    //ENDPINT PARA ACTUALIZAR CANTIDAD DE CUPOS POR PARTE DEL ADMIN
    @PATCH
    @Path("/{id}/cuposDisponibles")
    @Operation(
            summary = "Actualización manual de cupos (HU-21)",
            description = "Permite al administrador modificar manualmente los cupos disponibles de su parqueadero"
    )
    @APIResponse(
            responseCode = "200",
            description = "Cupos actualizados correctamente"
    )
    @APIResponse(
            responseCode = "400",
            description = "Supera la capacidad total o valor negativo"
    )
    @APIResponse(
            responseCode = "403",
            description = "No pertenece al administrador enviado"
    )
    @APIResponse(responseCode = "404",
            description = "Parqueadero no encontrado"
    )
    public Response actualizarCupos(
            @PathParam("id") Long id,
            @Valid ActualizarCuposDto dto) {

        try {
            parqueaderoServicio.actualizarCuposManualmente(id, dto.id(), dto.cuposDisponibles());
            return Response.ok("Cupos actualizados con éxito").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }
    }
}
