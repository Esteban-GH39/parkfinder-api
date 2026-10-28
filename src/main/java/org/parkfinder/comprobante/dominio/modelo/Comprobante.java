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

}
