package org.parkfinder.comprobante.dominio.repositorio;

import org.parkfinder.comprobante.dominio.modelo.Comprobante;

public interface ComprobanteRepositorio {

    //METODO PARA MOSTAR COMPROBANTE
    Comprobante mostrarComprobante(Long id);

    //METODO GENERAR EL COMPROBANTE DE UN PAGO APROBADO (HU-09). Su id es el código de reserva.
    Comprobante generar(Comprobante comprobante);
}
