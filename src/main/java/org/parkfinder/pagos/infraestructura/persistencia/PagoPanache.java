package org.parkfinder.pagos.infraestructura.persistencia;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.parkfinder.pagos.dominio.modelo.EstadoPago;
import org.parkfinder.pagos.dominio.modelo.Pago;
import org.parkfinder.pagos.dominio.modelo.PagoEntity;
import org.parkfinder.pagos.dominio.repositorio.PagoRepositorio;

@ApplicationScoped
public class PagoPanache implements PagoRepositorio, PanacheRepository<PagoEntity> {

    //MÉTODO PARA GUARDAR EL RESULTADO DE UN PAGO (HU-07)
    @Override
    @Transactional
    public Pago guardar(Pago pago) {
        PagoEntity entidad = PagoEntity
                .builder()
                .reservaId(pago.reservaId)
                .medioPago(pago.medioPago)
                .monto(pago.monto)
                .estado(pago.estado)
                .referencia(pago.referencia)
                .detalle(pago.detalle)
                .mensaje(pago.mensaje)
                .creadoEn(pago.creadoEn)
                .build();
        persist(entidad);
        pago.id = entidad.id;
        return pago;
    }

    @Override
    public boolean existeAprobado(Long reservaId) {
        return count("reservaId = ?1 and estado = ?2", reservaId, EstadoPago.APROBADO) > 0;
    }
}
