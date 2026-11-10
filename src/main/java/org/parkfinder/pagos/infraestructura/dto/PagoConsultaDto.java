package org.parkfinder.pagos.infraestructura.dto;

import org.parkfinder.pagos.dominio.modelo.EstadoPago;
import org.parkfinder.pagos.dominio.modelo.MedioPago;
import org.parkfinder.pagos.dominio.modelo.Pago;

import java.time.LocalDateTime;

public record PagoConsultaDto(
        Long id,
        Long reservaId,
        MedioPago medioPago,
        Double monto,
        EstadoPago estado,
        String referencia,
        String detalle,
        String mensaje,
        LocalDateTime creadoEn,
        Long codigoReserva
) {
    public static PagoConsultaDto de(Pago pago) {
        return new PagoConsultaDto(pago.id, pago.reservaId, pago.medioPago, pago.monto, pago.estado,
                pago.referencia, pago.detalle, pago.mensaje, pago.creadoEn, pago.comprobanteId);
    }
}
