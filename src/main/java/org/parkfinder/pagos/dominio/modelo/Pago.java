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
}
