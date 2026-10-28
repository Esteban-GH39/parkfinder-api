package org.parkfinder.comprobante.dominio.repositorio;

import org.parkfinder.comprobante.dominio.modelo.Comprobante;

public interface ComprobanteRepositorio {

    //METODO PARA MOSTAR COMPROBANTE
    Comprobante mostrarComprobante(Long id);
}
