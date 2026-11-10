package org.parkfinder.reservas.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoConsulta;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;
import org.parkfinder.reservas.dominio.excepcion.ReservaInvalidaException;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.modelo.ReservaDetalle;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@ApplicationScoped
public class ReservaServicio {

    @Inject
    ReservaRepositorio repositorio;

    @Inject
    ParqueaderoRepositorio parqueaderoRepositorio;

    @ConfigProperty(name = "parkfinder.reserva.retencion-minutos", defaultValue = "10")
    long retencionMinutos;

    @ConfigProperty(name = "parkfinder.pago.costo-reserva", defaultValue = "3000")
    double costoReserva;

    //MÉTODO RESERVAR UN CUPO DE CARRO (HU-06)
    public Reserva reservar(Long clienteId, Long parqueaderoId, LocalDateTime fechaHoraLlegada) {
        return reservar(clienteId, parqueaderoId, fechaHoraLlegada, 1, 0);
    }

    //MÉTODO RESERVAR VARIOS CUPOS DE CARRO A LA VEZ (HU-06)
    public Reserva reservar(Long clienteId, Long parqueaderoId, LocalDateTime fechaHoraLlegada, int carros) {
        return reservar(clienteId, parqueaderoId, fechaHoraLlegada, carros, 0);
    }

    //MÉTODO RESERVAR CUPOS DE CARRO Y DE MOTO EN UNA SOLA RESERVA: cada tipo se descuenta de su propio
    //contador y todo se paga junto. Si no llega ninguna cantidad se reserva un cupo de carro.
    public Reserva reservar(Long clienteId, Long parqueaderoId, LocalDateTime fechaHoraLlegada,
                            Integer carros, Integer motos) {
        int cantidadMotos = motos == null ? 0 : motos;
        int cantidadCarros = carros == null ? (cantidadMotos == 0 ? 1 : 0) : carros;
        if (cantidadCarros < 0 || cantidadMotos < 0 || cantidadCarros + cantidadMotos < 1) {
            throw new ReservaInvalidaException("Debes pedir al menos un cupo");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (!fechaHoraLlegada.isAfter(ahora)) {
            throw new ReservaInvalidaException("La fecha y hora de llegada debe ser futura");
        }

        // Lanza ParqueaderoNoEncontradoException si no existe
        ParqueaderoConsulta parqueadero = parqueaderoRepositorio.obtenerPorId(parqueaderoId);
        validarHorario(parqueadero, fechaHoraLlegada.toLocalTime());

        Reserva reserva = Reserva
                .builder()
                .clienteId(clienteId)
                .parqueaderoId(parqueaderoId)
                .cantidad(cantidadCarros + cantidadMotos)
                .cantidadMotos(cantidadMotos)
                .fechaHoraLlegada(fechaHoraLlegada)
                .estado(EstadoReserva.RETENIDA)
                .creadaEn(ahora)
                .expiraEn(ahora.plusMinutes(retencionMinutos))
                .build();
        return repositorio.retenerCupo(reserva);
    }

    //MÉTODO CONSULTAR UNA RESERVA (HU-06)
    public Reserva obtenerPorId(Long id) {
        return repositorio.obtenerPorId(id);
    }

    //MÉTODO VALIDAR EL CÓDIGO DE RESERVA: el cliente llegó (se anota la hora; sigue CONFIRMADA)
    public Reserva validar(Long id) {
        return repositorio.validar(id);
    }

    //MÉTODO LISTAR LAS RESERVAS CONFIRMADAS DE UN CLIENTE, con su código de reserva
    public List<ReservaDetalle> listarConfirmadasPorCliente(Long clienteId) {
        return repositorio.listarConfirmadasPorCliente(clienteId);
    }

    //MÉTODO COSTO FIJO DE CADA RESERVA (lo que se paga en la pasarela)
    public double costoReserva() {
        return costoReserva;
    }

    //MÉTODO LISTAR LAS RESERVAS PENDIENTES DE PAGO DE UN CLIENTE (HU-07)
    public List<ReservaDetalle> listarPendientesPorCliente(Long clienteId) {
        return repositorio.listarPendientesPorCliente(clienteId);
    }

    //MÉTODO LISTAR LAS RESERVAS DE LOS PARQUEADEROS DE UN DUEÑO
    public List<ReservaDetalle> listarPorAdmin(int adminId) {
        return repositorio.listarPorAdmin(adminId);
    }

    private void validarHorario(ParqueaderoConsulta parqueadero, LocalTime llegada) {
        LocalTime inicio = parqueadero.horaInicio;
        LocalTime fin = parqueadero.horaFin;
        if (inicio != null && fin != null && (llegada.isBefore(inicio) || llegada.isAfter(fin))) {
            throw new ReservaInvalidaException(
                    "El parqueadero atiende entre " + inicio + " y " + fin);
        }
    }
}
