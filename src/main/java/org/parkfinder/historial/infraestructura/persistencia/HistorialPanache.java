package org.parkfinder.historial.infraestructura.persistencia;

import jakarta.enterprise.context.ApplicationScoped;
import org.parkfinder.clientes.dominio.modelo.ClienteEntity;
import org.parkfinder.comprobante.dominio.modelo.ComprobanteEntity;
import org.parkfinder.estadias.dominio.modelo.EstadiaEntity;
import org.parkfinder.historial.dominio.modelo.CobroEstadia;
import org.parkfinder.historial.dominio.modelo.ReservaHistorial;
import org.parkfinder.historial.dominio.repositorio.HistorialRepositorio;
import org.parkfinder.pagos.dominio.modelo.EstadoPago;
import org.parkfinder.pagos.dominio.modelo.PagoEntity;
import org.parkfinder.reservas.dominio.modelo.ReservaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * HU-24: lee el historial de las tablas existentes (RESERVAS, PAGOS, COMPROBANTE, CLIENTES y ESTADIAS).
 * Clientes, pagos y comprobantes se traen en una consulta por tabla, no una por reserva.
 */
@ApplicationScoped
public class HistorialPanache implements HistorialRepositorio {

    //MÉTODO LISTAR LAS RESERVAS DEL PERIODO CON SU PAGO APROBADO
    @Override
    public List<ReservaHistorial> listarReservas(Map<Long, String> nombres, LocalDateTime desde, LocalDateTime hasta) {
        List<ReservaEntity> reservas = ReservaEntity.list(
                "parqueaderoId in ?1 and creadaEn >= ?2 and creadaEn < ?3 order by creadaEn desc",
                nombres.keySet(), desde, hasta);
        if (reservas.isEmpty()) {
            return List.of();
        }
        List<Long> reservaIds = reservas.stream().map(r -> r.id).toList();
        List<Long> clienteIds = reservas.stream().map(r -> r.clienteId).distinct().toList();

        Map<Long, ClienteEntity> clientes = ClienteEntity.<ClienteEntity>list("id in ?1", clienteIds).stream()
                .collect(Collectors.toMap(c -> c.id, Function.identity()));
        // Una reserva tiene a lo sumo un pago aprobado; si hubiera más se suman
        Map<Long, Double> pagado = PagoEntity.<PagoEntity>list("reservaId in ?1 and estado = ?2",
                        reservaIds, EstadoPago.APROBADO).stream()
                .collect(Collectors.groupingBy(p -> p.reservaId,
                        Collectors.summingDouble(p -> p.monto == null ? 0 : p.monto)));
        Map<Long, Long> codigos = ComprobanteEntity.<ComprobanteEntity>list("reservaId in ?1", reservaIds).stream()
                .collect(Collectors.toMap(c -> c.reservaId, c -> c.id, (a, b) -> a));

        return reservas.stream().map(r -> {
            ClienteEntity cliente = clientes.get(r.clienteId);
            return new ReservaHistorial(
                    r.id,
                    r.parqueaderoId,
                    nombres.get(r.parqueaderoId),
                    cliente != null ? cliente.nombre : null,
                    cliente != null ? cliente.correo : null,
                    r.cantidad,
                    r.cantidadMotos,
                    codigos.get(r.id),
                    r.estado,
                    r.creadaEn,
                    r.expiraEn,
                    r.fechaHoraLlegada,
                    r.validadaEn,
                    pagado.getOrDefault(r.id, 0.0));
        }).toList();
    }

    //MÉTODO LISTAR LOS VEHÍCULOS QUE SALIERON EN EL PERIODO CON LO COBRADO
    @Override
    public List<CobroEstadia> listarCobros(Map<Long, String> nombres, LocalDateTime desde, LocalDateTime hasta) {
        return EstadiaEntity.<EstadiaEntity>list(
                        "parqueaderoId in ?1 and salidaEn >= ?2 and salidaEn < ?3 order by salidaEn desc",
                        nombres.keySet(), desde, hasta).stream()
                .map(e -> new CobroEstadia(
                        e.id,
                        e.parqueaderoId,
                        nombres.get(e.parqueaderoId),
                        e.reservaId,
                        e.placa,
                        e.tipoVehiculo,
                        e.ingresoEn,
                        e.salidaEn,
                        e.minutos,
                        e.valorCobrado == null ? 0 : e.valorCobrado))
                .toList();
    }
}
