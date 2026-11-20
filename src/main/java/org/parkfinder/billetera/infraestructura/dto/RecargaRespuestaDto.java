package org.parkfinder.billetera.infraestructura.dto;

import org.parkfinder.billetera.aplicacion.BilleteraServicio;
import org.parkfinder.pagos.dominio.modelo.EstadoPago;

/** estado es APROBADO o RECHAZADO; movimiento viene null cuando se rechaza. */
public record RecargaRespuestaDto(EstadoPago estado, String mensaje, String referencia, long saldo,
                                  MovimientoDto movimiento) {

    public static RecargaRespuestaDto de(BilleteraServicio.Recarga r) {
        return new RecargaRespuestaDto(r.estado(), r.mensaje(), r.referencia(), r.saldo(),
                r.movimiento() == null ? null : MovimientoDto.de(r.movimiento()));
    }
}
