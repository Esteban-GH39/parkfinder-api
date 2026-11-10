package org.parkfinder.comprobante.dominio.modelo;
import lombok.Builder;

import java.time.LocalDate;


@Builder
public class Comprobante {
    public Long idComprobante;
    public LocalDate fechaGenerado;
    public Double valorPago;
    public String medioPago;
    public String estadoTransaccion;
    public Long fk_comprobante_cliente;
    public Long fk_comprobante_parqueadero;
    /** Reserva que pagó este comprobante; su id es el código de reserva. */
    public Long reservaId;

}
