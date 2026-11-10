package org.parkfinder.reservas.infraestructura.persistencia;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.parkfinder.clientes.dominio.modelo.ClienteEntity;
import org.parkfinder.parqueaderos.dominio.excepcion.ParqueaderoNoEncontradoException;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoEntity;
import org.parkfinder.comprobante.dominio.modelo.ComprobanteEntity;
import org.parkfinder.reservas.dominio.excepcion.ReservaEstadoInvalidoException;
import org.parkfinder.reservas.dominio.excepcion.ReservaInvalidaException;
import org.parkfinder.reservas.dominio.excepcion.ReservaNoEncontradaException;
import org.parkfinder.reservas.dominio.excepcion.SinCupoException;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.modelo.ReservaDetalle;
import org.parkfinder.reservas.dominio.modelo.ReservaEntity;
import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;
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

        int cantidad = reserva.cantidad == null ? 1 : reserva.cantidad;
        int motos = reserva.cantidadMotos == null ? 0 : reserva.cantidadMotos;
        int carros = cantidad - motos;
        // cuposDisponibles es el total; de él, cuposMotos son de motos y el resto de carros
        int cuposMotos = parqueadero.cuposMotos == null ? 0 : parqueadero.cuposMotos;
        int cuposCarros = Math.max(0, parqueadero.cuposDisponibles - cuposMotos);
        // Si falta de cualquiera de los dos tipos no se retiene nada
        if (carros > cuposCarros) {
            throw new SinCupoException(parqueadero.id, TipoVehiculo.CARRO, carros, cuposCarros);
        }
        if (motos > cuposMotos) {
            throw new SinCupoException(parqueadero.id, TipoVehiculo.MOTO, motos, cuposMotos);
        }
        parqueadero.cuposDisponibles -= cantidad;
        parqueadero.cuposMotos = cuposMotos - motos;

        ReservaEntity entidad = ReservaEntity
                .builder()
                .clienteId(reserva.clienteId)
                .parqueaderoId(reserva.parqueaderoId)
                .cantidad(cantidad)
                .cantidadMotos(motos)
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

    //MÉTODO PARA CONFIRMAR UNA RESERVA CON PAGO APROBADO (HU-07/HU-08)
    @Override
    @Transactional
    public void confirmar(Long id) {
        ReservaEntity entidad = findById(id, LockModeType.PESSIMISTIC_WRITE);
        if (entidad == null) {
            throw new ReservaNoEncontradaException(id);
        }
        // El pago ya se aprobó: se respeta aunque la retención venciera justo en ese instante
        if (entidad.estado == EstadoReserva.RETENIDA) {
            entidad.estado = EstadoReserva.CONFIRMADA;
        }
    }

    //MÉTODO PARA VALIDAR EL CÓDIGO DE RESERVA: se anota la hora; la reserva sigue CONFIRMADA
    @Override
    @Transactional
    public Reserva validar(Long id) {
        ReservaEntity entidad = findById(id, LockModeType.PESSIMISTIC_WRITE);
        if (entidad == null) {
            throw new ReservaNoEncontradaException(id);
        }
        if (entidad.estado != EstadoReserva.CONFIRMADA) {
            throw new ReservaEstadoInvalidoException(
                    "La reserva no está confirmada (estado " + entidad.estado + "): falta el pago o ya venció");
        }
        if (entidad.validadaEn != null) {
            throw new ReservaEstadoInvalidoException("Este código de reserva ya fue utilizado");
        }
        entidad.validadaEn = LocalDateTime.now();
        return aDominio(entidad);
    }

    //MÉTODO PARA LISTAR LAS RESERVAS CONFIRMADAS DE UN CLIENTE (con su código de reserva)
    @Override
    @Transactional
    public List<ReservaDetalle> listarConfirmadasPorCliente(Long clienteId) {
        return list("clienteId = ?1 and estado = ?2 and validadaEn is null order by creadaEn desc",
                clienteId, EstadoReserva.CONFIRMADA)
                .stream().map(r -> {
                    ParqueaderoEntity parqueadero = ParqueaderoEntity.findById(r.parqueaderoId);
                    ComprobanteEntity comprobante = ComprobanteEntity.find("reservaId", r.id).firstResult();
                    return ReservaDetalle
                            .builder()
                            .id(r.id)
                            .parqueaderoId(r.parqueaderoId)
                            .cantidad(r.cantidad)
                            .cantidadMotos(r.cantidadMotos)
                            .codigoReserva(comprobante != null ? comprobante.id : null)
                            .parqueaderoNombre(parqueadero != null ? parqueadero.nombre : null)
                            .fechaHoraLlegada(r.fechaHoraLlegada)
                            .estado(r.estado)
                            .creadaEn(r.creadaEn)
                            .expiraEn(r.expiraEn)
                            .build();
                }).toList();
    }

    //MÉTODO PARA LISTAR LAS RESERVAS PENDIENTES DE PAGO DE UN CLIENTE
    @Override
    @Transactional
    public List<ReservaDetalle> listarPendientesPorCliente(Long clienteId) {
        // Se libera lo vencido antes de listar, para que solo queden retenciones vigentes
        List<ReservaEntity> retenidas = list("clienteId = ?1 and estado = ?2", clienteId, EstadoReserva.RETENIDA);
        retenidas.stream().map(r -> r.parqueaderoId).distinct().forEach(parqueaderoId ->
                liberarVencidas(ParqueaderoEntity.findById(parqueaderoId, LockModeType.PESSIMISTIC_WRITE)));

        return list("clienteId = ?1 and estado = ?2 order by creadaEn desc", clienteId, EstadoReserva.RETENIDA)
                .stream().map(r -> {
                    ParqueaderoEntity parqueadero = ParqueaderoEntity.findById(r.parqueaderoId);
                    return ReservaDetalle
                            .builder()
                            .id(r.id)
                            .parqueaderoId(r.parqueaderoId)
                            .cantidad(r.cantidad)
                            .cantidadMotos(r.cantidadMotos)
                            .parqueaderoNombre(parqueadero != null ? parqueadero.nombre : null)
                            .tarifaHora(parqueadero != null ? parqueadero.tarifaHora : null)
                            .tarifaHoraMoto(parqueadero != null ? parqueadero.tarifaHoraMoto : null)
                            .fechaHoraLlegada(r.fechaHoraLlegada)
                            .estado(r.estado)
                            .creadaEn(r.creadaEn)
                            .expiraEn(r.expiraEn)
                            .build();
                }).toList();
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
            ComprobanteEntity comprobante = ComprobanteEntity.find("reservaId", r.id).firstResult();
            return ReservaDetalle
                    .builder()
                    .id(r.id)
                    .codigoReserva(comprobante != null ? comprobante.id : null)
                    .validadaEn(r.validadaEn)
                    .parqueaderoId(r.parqueaderoId)
                    .cantidad(r.cantidad)
                    .cantidadMotos(r.cantidadMotos)
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
            devolverCupos(parqueadero, vencida);
        }
    }

    /** Devuelve los cupos de la reserva, cada tipo a su contador, sin pasar de la capacidad de ese tipo. */
    private void devolverCupos(ParqueaderoEntity parqueadero, ReservaEntity reserva) {
        int cantidad = reserva.cantidad == null ? 1 : reserva.cantidad;
        int motos = reserva.cantidadMotos == null ? 0 : reserva.cantidadMotos;
        int carros = cantidad - motos;
        int capacidadMotos = parqueadero.capacidadMotos == null ? 0 : parqueadero.capacidadMotos;
        int cuposMotos = parqueadero.cuposMotos == null ? 0 : parqueadero.cuposMotos;
        int cuposCarros = Math.max(0, parqueadero.cuposDisponibles - cuposMotos);
        int capacidadCarros = parqueadero.capacidadTotal - capacidadMotos;

        int nuevosMotos = Math.min(capacidadMotos, cuposMotos + motos);
        int nuevosCarros = Math.min(capacidadCarros, cuposCarros + carros);
        parqueadero.cuposMotos = nuevosMotos;
        parqueadero.cuposDisponibles = nuevosCarros + nuevosMotos;
    }

    private Reserva aDominio(ReservaEntity entidad) {
        return Reserva
                .builder()
                .id(entidad.id)
                .clienteId(entidad.clienteId)
                .parqueaderoId(entidad.parqueaderoId)
                .cantidad(entidad.cantidad)
                .cantidadMotos(entidad.cantidadMotos)
                .validadaEn(entidad.validadaEn)
                .fechaHoraLlegada(entidad.fechaHoraLlegada)
                .estado(entidad.estado)
                .creadaEn(entidad.creadaEn)
                .expiraEn(entidad.expiraEn)
                .build();
    }
}
