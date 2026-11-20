package org.parkfinder.billetera.infraestructura.dto;

import org.parkfinder.billetera.dominio.modelo.Movimiento;
import org.parkfinder.billetera.dominio.modelo.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoDto(Long id, TipoMovimiento tipo, long monto, long saldoPosterior, Long reservaId,
                            String referencia, String detalle, LocalDateTime creadoEn) {

    public static MovimientoDto de(Movimiento m) {
        return new MovimientoDto(m.id, m.tipo, m.monto, m.saldoPosterior, m.reservaId, m.referencia,
                m.detalle, m.creadoEn);
    }
}
