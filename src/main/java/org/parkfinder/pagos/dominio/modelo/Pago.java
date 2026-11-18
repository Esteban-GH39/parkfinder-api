package org.parkfinder.pagos.dominio.modelo;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class Pago {
    public Long id;
    public Long reservaId;
    public MedioPago medioPago;
    public Double monto;
    public EstadoPago estado;
    public String referencia;
    public String detalle;
    public String mensaje;
    public LocalDateTime creadoEn;
    /** Código de reserva: id del comprobante generado al aprobarse. No se guarda en el pago. */
    public Long comprobanteId;
    /** Intentos que le quedan a la reserva tras un rechazo (HU-08). No se guarda en el pago. */
    public Integer intentosRestantes;
    /** true si este rechazo agotó los intentos y la reserva se canceló liberando el cupo (HU-08). */
    public boolean reservaCancelada;
}
