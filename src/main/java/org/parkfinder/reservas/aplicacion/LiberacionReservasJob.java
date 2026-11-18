package org.parkfinder.reservas.aplicacion;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * HU-11: cada cierto tiempo cancela las reservas pendientes de pago cuya retención venció, para que el
 * cupo vuelva a estar disponible aunque nadie consulte el parqueadero. No corre dos veces a la vez.
 */
@ApplicationScoped
public class LiberacionReservasJob {

    private static final Logger LOG = Logger.getLogger(LiberacionReservasJob.class);

    @Inject
    ReservaServicio reservaServicio;

    @Scheduled(every = "{parkfinder.reserva.liberacion-cada:30s}",
            concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void liberarVencidas() {
        int liberadas = reservaServicio.liberarVencidas();
        if (liberadas > 0) {
            LOG.infof("HU-11: %d reservas sin pagar liberadas", liberadas);
        }
    }
}
