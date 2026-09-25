package org.parkfinder.parqueaderos.infraestructura.persistencia;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoEntity;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;

import java.util.List;

@ApplicationScoped
public class ParqueaderoPanache implements ParqueaderoRepositorio, PanacheRepository<ParqueaderoEntity> {
    //MÉTODO PARA CREAR PARQUEADERO
    @Override
    @Transactional
    public void crearPark(Parqueadero parqueadero){
        ParqueaderoEntity parqueaderoEntity = ParqueaderoEntity
                .builder()
                .nombre(parqueadero.nombre)
                .direccion(parqueadero.direccion)
                .ubicacion(parqueadero.ubicacion)
                .capacidadTotal(parqueadero.capacidadTotal)
                .tarifa(parqueadero.tarifa)
                .horaInicio(parqueadero.horaInicio)
                .horaFin(parqueadero.horaFin)
                .nombrePropietario(parqueadero.nombrePropietario)
                .build();
        persist(parqueaderoEntity);
    }



}
