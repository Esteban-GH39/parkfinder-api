package org.parkfinder.reservas.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoConsulta;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;
import org.parkfinder.reservas.dominio.excepcion.ReservaInvalidaException;
import org.parkfinder.reservas.dominio.modelo.EstadoReserva;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import java.time.LocalDateTime;
import java.time.LocalTime;

@ApplicationScoped
public class ReservaServicio {

    @Inject
    ReservaRepositorio repositorio;

    @Inject
    ParqueaderoRepositorio parqueaderoRepositorio;

    @ConfigProperty(name = "parkfinder.reserva.retencion-minutos", defaultValue = "10")
    long retencionMinutos;

    //MÉTODO RESERVAR UN CUPO (HU-06)
    public Reserva reservar(Long clienteId, Long parqueaderoId, LocalDateTime fechaHoraLlegada) {
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

    private void validarHorario(ParqueaderoConsulta parqueadero, LocalTime llegada) {
        LocalTime inicio = parqueadero.horaInicio;
        LocalTime fin = parqueadero.horaFin;
        if (inicio != null && fin != null && (llegada.isBefore(inicio) || llegada.isAfter(fin))) {
            throw new ReservaInvalidaException(
                    "El parqueadero atiende entre " + inicio + " y " + fin);
        }
    }
}
