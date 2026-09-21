package com.parkfinder.parqueaderos;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/v1/parqueaderos")
@Produces(MediaType.APPLICATION_JSON)
public class ParqueaderoResource {

    @GET
    public List<Parqueadero> buscar(@QueryParam("zona") String zona,
                                    @QueryParam("direccion") String direccion) {
        // Criterio de aceptación HU-03: buscar por zona o dirección
        if (zona != null && !zona.isBlank()) {
            return Parqueadero.list("zona", zona);
        }
        if (direccion != null && !direccion.isBlank()) {
            return Parqueadero.list("direccion like ?1", "%" + direccion + "%");
        }
        return Parqueadero.listAll();
        // Ordenar por distancia (GPS) se agrega cuando tengamos lat/lng del cliente
    }
}