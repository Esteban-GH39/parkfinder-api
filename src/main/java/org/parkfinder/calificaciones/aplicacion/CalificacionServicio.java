package org.parkfinder.calificaciones.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.parkfinder.calificaciones.dominio.excepcion.CalificacionDuplicadaException;
import org.parkfinder.calificaciones.dominio.excepcion.CalificacionInvalidaException;
import org.parkfinder.calificaciones.dominio.excepcion.CalificacionNoPermitidaException;
import org.parkfinder.calificaciones.dominio.modelo.Calificacion;
import org.parkfinder.calificaciones.dominio.modelo.ResumenCalificaciones;
import org.parkfinder.calificaciones.dominio.repositorio.CalificacionRepositorio;

@ApplicationScoped
public class CalificacionServicio {

    public static final int MAX_COMENTARIO = 500;

    @Inject
    CalificacionRepositorio repositorio;

    //MÉTODO CALIFICAR UN PARQUEADERO (HU-13). Estrellas de 1 a 5, comentario opcional, y solo si el
    //cliente completó una reserva en él. Cada cliente califica una sola vez cada parqueadero.
    public Calificacion calificar(Long clienteId, Long parqueaderoId, int estrellas, String comentario) {
        if (estrellas < 1 || estrellas > 5) {
            throw new CalificacionInvalidaException("La calificación debe ser de 1 a 5 estrellas");
        }
        String texto = comentario == null || comentario.isBlank() ? null : comentario.trim();
        if (texto != null && texto.length() > MAX_COMENTARIO) {
            throw new CalificacionInvalidaException(
                    "El comentario no puede superar " + MAX_COMENTARIO + " caracteres");
        }
        if (!repositorio.existeCliente(clienteId)) {
            throw new CalificacionInvalidaException("No existe un cliente con id " + clienteId);
        }
        if (!repositorio.existeParqueadero(parqueaderoId)) {
            throw new CalificacionInvalidaException("No existe un parqueadero con id " + parqueaderoId);
        }
        if (!repositorio.clienteCompletoReserva(clienteId, parqueaderoId)) {
            throw new CalificacionNoPermitidaException();
        }
        if (repositorio.yaCalifico(clienteId, parqueaderoId)) {
            throw new CalificacionDuplicadaException();
        }
        return repositorio.crear(Calificacion
                .builder()
                .clienteId(clienteId)
                .parqueaderoId(parqueaderoId)
                .estrellas(estrellas)
                .comentario(texto)
                .build());
    }

    //MÉTODO PROMEDIO Y COMENTARIOS DE UN PARQUEADERO (HU-13, para el detalle de HU-05)
    public ResumenCalificaciones resumen(Long parqueaderoId) {
        return repositorio.resumenPorParqueadero(parqueaderoId);
    }

    //MÉTODO SABER SI EL CLIENTE PUEDE CALIFICAR AHORA (para mostrar u ocultar el botón en la app)
    public boolean puedeCalificar(Long clienteId, Long parqueaderoId) {
        return repositorio.clienteCompletoReserva(clienteId, parqueaderoId)
                && !repositorio.yaCalifico(clienteId, parqueaderoId);
    }

    public boolean yaCalifico(Long clienteId, Long parqueaderoId) {
        return repositorio.yaCalifico(clienteId, parqueaderoId);
    }
}
