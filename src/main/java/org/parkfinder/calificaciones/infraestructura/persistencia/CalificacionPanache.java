package org.parkfinder.calificaciones.infraestructura.persistencia;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.parkfinder.calificaciones.dominio.modelo.Calificacion;
import org.parkfinder.calificaciones.dominio.modelo.CalificacionEntity;
import org.parkfinder.calificaciones.dominio.modelo.ResumenCalificaciones;
import org.parkfinder.calificaciones.dominio.repositorio.CalificacionRepositorio;
import org.parkfinder.clientes.dominio.modelo.ClienteEntity;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoEntity;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CalificacionPanache implements CalificacionRepositorio, PanacheRepository<CalificacionEntity> {

    @Override
    public boolean clienteCompletoReserva(Long clienteId, Long parqueaderoId) {
        Long completadas = getEntityManager()
                .createQuery("select count(e) from EstadiaEntity e, ReservaEntity r "
                        + "where e.reservaId = r.id and r.clienteId = ?1 and r.parqueaderoId = ?2 "
                        + "and e.salidaEn is not null", Long.class)
                .setParameter(1, clienteId)
                .setParameter(2, parqueaderoId)
                .getSingleResult();
        return completadas > 0;
    }

    @Override
    public boolean yaCalifico(Long clienteId, Long parqueaderoId) {
        return count("clienteId = ?1 and parqueaderoId = ?2", clienteId, parqueaderoId) > 0;
    }

    @Override
    public boolean existeCliente(Long clienteId) {
        return ClienteEntity.findById(clienteId) != null;
    }

    @Override
    public boolean existeParqueadero(Long parqueaderoId) {
        return ParqueaderoEntity.findById(parqueaderoId) != null;
    }

    @Override
    @Transactional
    public Calificacion crear(Calificacion calificacion) {
        CalificacionEntity entidad = CalificacionEntity
                .builder()
                .parqueaderoId(calificacion.parqueaderoId)
                .clienteId(calificacion.clienteId)
                .estrellas(calificacion.estrellas)
                .comentario(calificacion.comentario)
                .creadaEn(LocalDateTime.now())
                .build();
        persist(entidad);
        return aDominio(entidad);
    }

    @Override
    public ResumenCalificaciones resumenPorParqueadero(Long parqueaderoId) {
        List<CalificacionEntity> entidades =
                list("parqueaderoId = ?1 order by creadaEn desc", parqueaderoId);
        List<Calificacion> calificaciones = entidades.stream().map(this::aDominio).toList();

        Double promedio = null;
        if (!entidades.isEmpty()) {
            double suma = entidades.stream().mapToInt(e -> e.estrellas).sum();
            promedio = Math.round(suma / entidades.size() * 10.0) / 10.0;
        }
        return ResumenCalificaciones
                .builder()
                .promedio(promedio)
                .total(entidades.size())
                .calificaciones(calificaciones)
                .build();
    }

    private Calificacion aDominio(CalificacionEntity e) {
        ClienteEntity cliente = ClienteEntity.findById(e.clienteId);
        return Calificacion
                .builder()
                .id(e.id)
                .parqueaderoId(e.parqueaderoId)
                .clienteId(e.clienteId)
                .clienteNombre(cliente != null ? cliente.nombre : null)
                .estrellas(e.estrellas)
                .comentario(e.comentario)
                .creadaEn(e.creadaEn)
                .build();
    }
}
