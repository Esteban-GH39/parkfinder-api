package org.parkfinder.comprobante.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.parkfinder.comprobante.dominio.modelo.Comprobante;
import org.parkfinder.comprobante.dominio.repositorio.ComprobanteRepositorio;

@ApplicationScoped
public class ComprobanteServicio {

    @Inject
    ComprobanteRepositorio repositorio;
    //METODO PARA MOSTAR COMPROBANTE
    public Comprobante obtenerPorId(Long id){
        return repositorio.mostrarComprobante(id);
    }
}
