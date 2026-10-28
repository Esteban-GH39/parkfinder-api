package org.parkfinder.reservas.infraestructura.persistencia;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.parkfinder.clientes.dominio.modelo.ClienteEntity;
import org.parkfinder.parqueaderos.dominio.excepcion.ParqueaderoNoEncontradoException;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoEntity;
import org.parkfinder.reservas.dominio.excepcion.ReservaInvalidaException;
import org.parkfinder.reservas.dominio.excepcion.ReservaNoEncontradaException;
import org.parkfinder.reservas.dominio.excepcion.SinCupoException;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.modelo.ReservaDetalle;
import org.parkfinder.reservas.dominio.modelo.ReservaEntity;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReservaPanache implements ReservaRepositorio, PanacheRepository<ReservaEntity> {

    //MÉTODO PARA RETENER UN CUPO (HU-06)
    @Override
    @Transactional
    public Reserva retenerCupo(Reserva reserva) {
        // El bloqueo de la fila serializa las reservas del mismo parqueadero: dos clientes
        // no pueden llevarse el último cupo a la vez.
        ParqueaderoEntity parqueadero =
                ParqueaderoEntity.findById(reserva.parqueaderoId, LockModeType.PESSIMISTIC_WRITE);
        if (parqueadero == null) {
            throw new ParqueaderoNoEncontradoException(reserva.parqueaderoId);
        }
        if (ClienteEntity.findById(reserva.clienteId) == null) {
            throw new ReservaInvalidaException("No existe un cliente con id " + reserva.clienteId);
        }

        liberarVencidas(parqueadero);

        if (parqueadero.cuposDisponibles <= 0) {
            throw new SinCupoException(parqueadero.id);
        }
        parqueadero.cuposDisponibles--;

        ReservaEntity entidad = ReservaEntity
                .builder()
                .clienteId(reserva.clienteId)
                .parqueaderoId(reserva.parqueaderoId)
                .fechaHoraLlegada(reserva.fechaHoraLlegada)
                .estado(EstadoReserva.RETENIDA)
                .creadaEn(reserva.creadaEn)
                .expiraEn(reserva.expiraEn)
                .build();
        persist(entidad);
        return aDominio(entidad);
    }

    //MÉTODO PARA CONSULTAR UNA RESERVA (HU-06)
    @Override
    @Transactional
    public Reserva obtenerPorId(Long id) {
        ReservaEntity entidad = findById(id);
        if (entidad == null) {
            throw new ReservaNoEncontradaException(id);
        }
        if (entidad.estado == EstadoReserva.RETENIDA && entidad.expiraEn.isBefore(LocalDateTime.now())) {
            ParqueaderoEntity parqueadero =
                    ParqueaderoEntity.findById(entidad.parqueaderoId, LockModeType.PESSIMISTIC_WRITE);
            liberarVencidas(parqueadero);
        }
        return aDominio(entidad);
    }

    //MÉTODO PARA LISTAR LAS RESERVAS DE LOS PARQUEADEROS DE UN DUEÑO
    @Override
    @Transactional
    public List<ReservaDetalle> listarPorAdmin(int adminId) {
        List<ParqueaderoEntity> parqueaderos = ParqueaderoEntity.list("fk_parqueadero_admin", adminId);
        if (parqueaderos.isEmpty()) {
            return List.of();
        }
        // Se libera lo vencido antes de listar, para que estado y cupos cuenten lo mismo
        for (ParqueaderoEntity p : parqueaderos) {
            liberarVencidas(ParqueaderoEntity.findById(p.id, LockModeType.PESSIMISTIC_WRITE));
        }
        Map<Long, String> nombres = parqueaderos.stream()
                .collect(Collectors.toMap(p -> p.id, p -> p.nombre));

        List<ReservaEntity> reservas = list("parqueaderoId in ?1 order by creadaEn desc", nombres.keySet());
        return reservas.stream().map(r -> {
            ClienteEntity cliente = ClienteEntity.findById(r.clienteId);
            return ReservaDetalle
                    .builder()
                    .id(r.id)
                    .parqueaderoId(r.parqueaderoId)
                    .parqueaderoNombre(nombres.get(r.parqueaderoId))
                    .clienteNombre(cliente != null ? cliente.nombre : null)
                    .clienteCorreo(cliente != null ? cliente.correo : null)
                    .fechaHoraLlegada(r.fechaHoraLlegada)
                    .estado(r.estado)
                    .creadaEn(r.creadaEn)
                    .expiraEn(r.expiraEn)
                    .build();
        }).toList();
    }

    /**
     * La retención vence sin tarea programada: se libera cuando alguien reserva o consulta.
     * Debe llamarse con la fila del parqueadero ya bloqueada.
     */
    private void liberarVencidas(ParqueaderoEntity parqueadero) {
        List<ReservaEntity> vencidas = list("parqueaderoId = ?1 and estado = ?2 and expiraEn < ?3",
                parqueadero.id, EstadoReserva.RETENIDA, LocalDateTime.now());
        for (ReservaEntity vencida : vencidas) {
            vencida.estado = EstadoReserva.EXPIRADA;
            parqueadero.cuposDisponibles =
                    Math.min(parqueadero.capacidadTotal, parqueadero.cuposDisponibles + 1);
        }
    }

    private Reserva aDominio(ReservaEntity entidad) {
        return Reserva
                .builder()
                .id(entidad.id)
                .clienteId(entidad.clienteId)
                .parqueaderoId(entidad.parqueaderoId)
                .fechaHoraLlegada(entidad.fechaHoraLlegada)
                .estado(entidad.estado)
                .creadaEn(entidad.creadaEn)
                .expiraEn(entidad.expiraEn)
                .build();
    }
}
