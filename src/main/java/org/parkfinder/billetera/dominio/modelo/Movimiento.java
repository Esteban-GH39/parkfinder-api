package org.parkfinder.billetera.dominio.modelo;

import lombok.Builder;

import java.time.LocalDateTime;

/** Una línea del extracto de la billetera. Los valores van en pesos enteros. */
@Builder
public class Movimiento {
    public Long id;
    public Long clienteId;
    public TipoMovimiento tipo;
    /** Con signo: positivo si entra plata a la billetera, negativo si sale. */
    public long monto;
    public long saldoPosterior;
    /** Reserva a la que se refiere (pago o reembolso); null en las recargas. */
    public Long reservaId;
    public String referencia;
    public String detalle;
    public LocalDateTime creadoEn;
}
