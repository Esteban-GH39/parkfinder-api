package org.parkfinder.comprobante.infraestructura.persistencia;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.parkfinder.comprobante.dominio.modelo.Comprobante;
import org.parkfinder.comprobante.dominio.modelo.ComprobanteEntity;
import org.parkfinder.comprobante.dominio.repositorio.ComprobanteRepositorio;

@ApplicationScoped
public class ComprobantePanache implements ComprobanteRepositorio,PanacheRepository<ComprobanteEntity>  {

    //METODO PARA MOSTRAR COMPROBANTE POR ID
    @Override
    public Comprobante mostrarComprobante(Long id){
        ComprobanteEntity e = findById(id);
        if (e == null) {
            return null;
        }
        return Comprobante.builder()
                .idComprobante(e.id)
                .fechaGenerado(e.fechaGenerado)
                .valorPago(e.valorPago)
                .medioPago(e.medioPago)
                .estadoTransaccion(e.estadoTransaccion)
                .fk_comprobante_cliente(e.cliente != null ? e.cliente.id : null)
                .fk_comprobante_parqueadero(e.parqueadero != null ? e.parqueadero.id : null)
                .build();
    }
}
