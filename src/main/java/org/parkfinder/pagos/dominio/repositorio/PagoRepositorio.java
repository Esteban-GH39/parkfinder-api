package org.parkfinder.pagos.dominio.repositorio;

import org.parkfinder.pagos.dominio.modelo.Pago;

public interface PagoRepositorio {

    //METODO GUARDAR EL RESULTADO DE UN PAGO (HU-07), aprobado o rechazado
    Pago guardar(Pago pago);

    //METODO SABER SI LA RESERVA YA TIENE UN PAGO APROBADO
    boolean existeAprobado(Long reservaId);
}
