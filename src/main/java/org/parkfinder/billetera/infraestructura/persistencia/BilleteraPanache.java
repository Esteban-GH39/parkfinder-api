package org.parkfinder.billetera.infraestructura.persistencia;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.parkfinder.billetera.dominio.excepcion.SaldoInsuficienteException;
import org.parkfinder.billetera.dominio.modelo.BilleteraEntity;
import org.parkfinder.billetera.dominio.modelo.Movimiento;
import org.parkfinder.billetera.dominio.modelo.MovimientoEntity;
import org.parkfinder.billetera.dominio.modelo.TipoMovimiento;
import org.parkfinder.billetera.dominio.repositorio.BilleteraRepositorio;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class BilleteraPanache implements BilleteraRepositorio {

    @Override
    public long saldoDe(Long clienteId) {
        BilleteraEntity billetera = BilleteraEntity.find("clienteId", clienteId).firstResult();
        return billetera == null ? 0 : billetera.saldo;
    }

    @Override
    @Transactional
    public Movimiento acreditar(Long clienteId, TipoMovimiento tipo, long monto, Long reservaId,
                                String referencia, String detalle) {
        BilleteraEntity billetera = bloqueada(clienteId);
        billetera.saldo += monto;
        return registrar(clienteId, tipo, monto, billetera.saldo, reservaId, referencia, detalle);
    }

    @Override
    @Transactional
    public Movimiento debitar(Long clienteId, long monto, Long reservaId, String referencia, String detalle) {
        BilleteraEntity billetera = bloqueada(clienteId);
        if (billetera.saldo < monto) {
            throw new SaldoInsuficienteException("Saldo insuficiente: tienes " + pesos(billetera.saldo)
                    + " y la reserva cuesta " + pesos(monto));
        }
        billetera.saldo -= monto;
        return registrar(clienteId, TipoMovimiento.PAGO_RESERVA, -monto, billetera.saldo,
                reservaId, referencia, detalle);
    }

    @Override
    public List<Movimiento> movimientos(Long clienteId, int limite) {
        List<MovimientoEntity> filas = MovimientoEntity
                .<MovimientoEntity>find("clienteId = ?1 order by id desc", clienteId)
                .page(0, limite)
                .list();
        return filas.stream().map(this::aDominio).toList();
    }

    @Override
    public boolean existeMovimiento(Long reservaId, TipoMovimiento tipo) {
        return MovimientoEntity.count("reservaId = ?1 and tipo = ?2", reservaId, tipo) > 0;
    }

    // Toma la fila del cliente con bloqueo de escritura, para que dos operaciones a la vez (por ejemplo
    // dos pagos) no lean el mismo saldo. Si todavía no existe se crea; el flush fuerza el INSERT ahora:
    // si otra transacción la creó al mismo tiempo, falla aquí por la restricción única y el cliente reintenta.
    private BilleteraEntity bloqueada(Long clienteId) {
        BilleteraEntity billetera = BilleteraEntity
                .<BilleteraEntity>find("clienteId", clienteId)
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResult();
        if (billetera == null) {
            billetera = BilleteraEntity.builder().clienteId(clienteId).saldo(0).build();
            billetera.persist();
            billetera.flush();
        }
        return billetera;
    }

    private Movimiento registrar(Long clienteId, TipoMovimiento tipo, long monto, long saldoPosterior,
                                 Long reservaId, String referencia, String detalle) {
        MovimientoEntity entidad = MovimientoEntity.builder()
                .clienteId(clienteId)
                .tipo(tipo)
                .monto(monto)
                .saldoPosterior(saldoPosterior)
                .reservaId(reservaId)
                .referencia(referencia)
                .detalle(detalle)
                .creadoEn(LocalDateTime.now())
                .build();
        entidad.persist();
        return aDominio(entidad);
    }

    private Movimiento aDominio(MovimientoEntity e) {
        return Movimiento.builder()
                .id(e.id)
                .clienteId(e.clienteId)
                .tipo(e.tipo)
                .monto(e.monto)
                .saldoPosterior(e.saldoPosterior)
                .reservaId(e.reservaId)
                .referencia(e.referencia)
                .detalle(e.detalle)
                .creadoEn(e.creadoEn)
                .build();
    }

    private static String pesos(long valor) {
        return "$" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO")).format(valor);
    }
}
