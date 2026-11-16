package org.parkfinder.historial.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.parkfinder.historial.dominio.excepcion.HistorialException;
import org.parkfinder.historial.dominio.modelo.CobroEstadia;
import org.parkfinder.historial.dominio.modelo.Historial;
import org.parkfinder.historial.dominio.modelo.IngresoDia;
import org.parkfinder.historial.dominio.modelo.Periodo;
import org.parkfinder.historial.dominio.modelo.ReservaHistorial;
import org.parkfinder.historial.dominio.repositorio.HistorialRepositorio;
import org.parkfinder.parqueaderos.dominio.excepcion.ParqueaderoNoEncontradoException;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * HU-24: historial de reservas e ingresos de los parqueaderos de un dueño, filtrado por día, semana o mes.
 * Los ingresos salen de las tablas que ya existen: el pago aprobado de cada reserva (PAGOS) y lo cobrado
 * a cada vehículo al salir (ESTADIAS).
 */
@ApplicationScoped
public class HistorialServicio {

    @Inject
    HistorialRepositorio repositorio;

    @Inject
    ParqueaderoRepositorio parqueaderoRepositorio;

    /** Hora actual; las pruebas la reemplazan para fijar el tiempo. */
    Supplier<LocalDateTime> reloj = LocalDateTime::now;

    //MÉTODO CONSULTAR EL HISTORIAL DEL PERIODO QUE CONTIENE LA FECHA (hoy si no llega).
    //Sin parqueaderoId incluye todos los parqueaderos del dueño.
    public Historial consultar(int adminId, Long parqueaderoId, String periodoTexto, String fechaTexto) {
        Periodo periodo = periodoTexto == null || periodoTexto.isBlank() ? Periodo.DIA : Periodo.de(periodoTexto);
        if (periodo == null) {
            throw HistorialException.invalida("El periodo debe ser DIA, SEMANA o MES");
        }
        LocalDate fecha = leerFecha(fechaTexto);
        Periodo.Rango rango = periodo.rango(fecha);

        Map<Long, String> nombres = parqueaderosDelDueno(adminId, parqueaderoId);
        LocalDateTime desde = rango.desde().atStartOfDay();
        LocalDateTime hasta = rango.hasta().plusDays(1).atStartOfDay();

        LocalDateTime ahora = reloj.get();
        List<ReservaHistorial> reservas = nombres.isEmpty() ? List.of()
                : repositorio.listarReservas(nombres, desde, hasta).stream()
                .map(r -> retencionVencida(r, ahora) ? r.conEstado(EstadoReserva.EXPIRADA) : r)
                .toList();
        List<CobroEstadia> cobros = nombres.isEmpty() ? List.of()
                : repositorio.listarCobros(nombres, desde, hasta);

        double ingresosReservas = reservas.stream().mapToDouble(ReservaHistorial::montoPagado).sum();
        double ingresosEstadias = cobros.stream().mapToDouble(CobroEstadia::valorCobrado).sum();
        int pagadas = (int) reservas.stream().filter(r -> r.montoPagado() > 0).count();

        return new Historial(periodo, fecha, rango.desde(), rango.hasta(), parqueaderoId,
                ingresosReservas, ingresosEstadias, ingresosReservas + ingresosEstadias,
                reservas.size(), pagadas, cobros.size(),
                desglosePorDia(rango, reservas, cobros), reservas, cobros);
    }

    /** Ingresos de cada día del periodo, también los días sin movimiento (en 0). */
    private static List<IngresoDia> desglosePorDia(Periodo.Rango rango, List<ReservaHistorial> reservas,
                                                   List<CobroEstadia> cobros) {
        Map<LocalDate, double[]> dias = new LinkedHashMap<>();
        for (LocalDate d = rango.desde(); !d.isAfter(rango.hasta()); d = d.plusDays(1)) {
            dias.put(d, new double[2]);
        }
        for (ReservaHistorial r : reservas) {
            double[] dia = dias.get(r.creadaEn().toLocalDate());
            if (dia != null) {
                dia[0] += r.montoPagado();
            }
        }
        for (CobroEstadia c : cobros) {
            double[] dia = dias.get(c.salidaEn().toLocalDate());
            if (dia != null) {
                dia[1] += c.valorCobrado();
            }
        }
        List<IngresoDia> desglose = new ArrayList<>();
        dias.forEach((fecha, v) -> desglose.add(new IngresoDia(fecha, v[0], v[1], v[0] + v[1])));
        return desglose;
    }

    /** Nombre de cada parqueadero a consultar: uno solo si llega el id (debe ser del dueño) o todos los suyos. */
    private Map<Long, String> parqueaderosDelDueno(int adminId, Long parqueaderoId) {
        Map<Long, String> nombres = new LinkedHashMap<>();
        if (parqueaderoId != null) {
            Parqueadero parqueadero = parqueaderoRepositorio.buscarPorId(parqueaderoId)
                    .orElseThrow(() -> new ParqueaderoNoEncontradoException(parqueaderoId));
            if (parqueadero.fk_parqueadero_admin != adminId) {
                throw HistorialException.noPermitida("No tienes permisos sobre este parqueadero");
            }
            nombres.put(parqueadero.id, parqueadero.nombre);
            return nombres;
        }
        for (Parqueadero p : parqueaderoRepositorio.listarPorAdmin(adminId)) {
            nombres.put(p.id, p.nombre);
        }
        return nombres;
    }

    private LocalDate leerFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return reloj.get().toLocalDate();
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (RuntimeException e) {
            throw HistorialException.invalida("La fecha debe tener el formato AAAA-MM-DD");
        }
    }

    /** Una retención vencida que nadie ha liberado todavía se muestra como EXPIRADA. */
    private static boolean retencionVencida(ReservaHistorial r, LocalDateTime ahora) {
        return r.estado() == EstadoReserva.RETENIDA && r.expiraEn() != null && r.expiraEn().isBefore(ahora);
    }
}
