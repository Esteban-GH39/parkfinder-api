package org.parkfinder.estadias.infraestructura.persistencia;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.parkfinder.estadias.dominio.excepcion.EstadiaException;
import org.parkfinder.estadias.dominio.modelo.Estadia;
import org.parkfinder.estadias.dominio.modelo.EstadiaEntity;
import org.parkfinder.estadias.dominio.repositorio.EstadiaRepositorio;
import org.parkfinder.parqueaderos.dominio.excepcion.ParqueaderoNoEncontradoException;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoEntity;
import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EstadiaPanache implements EstadiaRepositorio, PanacheRepository<EstadiaEntity> {

    //MÉTODO PARA REGISTRAR EL INGRESO DE UN VEHÍCULO
    @Override
    @Transactional
    public Estadia registrarIngreso(Estadia estadia, boolean descontarCupo) {
        if (descontarCupo) {
            // El bloqueo de la fila evita que dos ingresos a la vez se lleven el último cupo
            ParqueaderoEntity parqueadero =
                    ParqueaderoEntity.findById(estadia.parqueaderoId, LockModeType.PESSIMISTIC_WRITE);
            if (parqueadero == null) {
                throw new ParqueaderoNoEncontradoException(estadia.parqueaderoId);
            }
            int cuposMotos = parqueadero.cuposMotos == null ? 0 : parqueadero.cuposMotos;
            int cuposCarros = Math.max(0, parqueadero.cuposDisponibles - cuposMotos);
            if (estadia.tipoVehiculo == TipoVehiculo.MOTO) {
                if (cuposMotos <= 0) {
                    throw EstadiaException.conflicto("No hay cupos de moto disponibles");
                }
                parqueadero.cuposMotos = cuposMotos - 1;
            } else if (cuposCarros <= 0) {
                throw EstadiaException.conflicto("No hay cupos de carro disponibles");
            }
            parqueadero.cuposDisponibles -= 1;
        }

        EstadiaEntity entidad = EstadiaEntity
                .builder()
                .parqueaderoId(estadia.parqueaderoId)
                .reservaId(estadia.reservaId)
                .placa(estadia.placa)
                .tipoVehiculo(estadia.tipoVehiculo)
                .ingresoEn(estadia.ingresoEn)
                .build();
        persist(entidad);
        return aDominio(entidad);
    }

    //MÉTODO PARA BUSCAR EL VEHÍCULO QUE TODAVÍA NO HA SALIDO
    @Override
    public Optional<Estadia> buscarActivaPorPlaca(String placa) {
        return find("placa = ?1 and salidaEn is null", placa).firstResultOptional().map(this::aDominio);
    }

    //MÉTODO PARA LISTAR LOS VEHÍCULOS DENTRO DE UN PARQUEADERO
    @Override
    public List<Estadia> listarActivas(Long parqueaderoId) {
        return list("parqueaderoId = ?1 and salidaEn is null order by ingresoEn", parqueaderoId)
                .stream().map(this::aDominio).toList();
    }

    //MÉTODO PARA REGISTRAR LA SALIDA Y DEVOLVER EL CUPO
    @Override
    @Transactional
    public Estadia registrarSalida(Long estadiaId, LocalDateTime salidaEn, long minutos, double valorCobrado) {
        EstadiaEntity entidad = findById(estadiaId, LockModeType.PESSIMISTIC_WRITE);
        if (entidad == null) {
            throw EstadiaException.noEncontrada("No existe el registro de ingreso " + estadiaId);
        }
        if (entidad.salidaEn != null) {
            throw EstadiaException.conflicto("El vehículo " + entidad.placa + " ya salió del parqueadero");
        }
        entidad.salidaEn = salidaEn;
        entidad.minutos = minutos;
        entidad.valorCobrado = valorCobrado;

        ParqueaderoEntity parqueadero =
                ParqueaderoEntity.findById(entidad.parqueaderoId, LockModeType.PESSIMISTIC_WRITE);
        if (parqueadero != null) {
            devolverCupo(parqueadero, entidad.tipoVehiculo);
        }
        return aDominio(entidad);
    }

    /** Devuelve un cupo del tipo, sin pasar de la capacidad de ese tipo. */
    private void devolverCupo(ParqueaderoEntity parqueadero, TipoVehiculo tipo) {
        int capacidadMotos = parqueadero.capacidadMotos == null ? 0 : parqueadero.capacidadMotos;
        int capacidadCarros = parqueadero.capacidadTotal - capacidadMotos;
        int cuposMotos = parqueadero.cuposMotos == null ? 0 : parqueadero.cuposMotos;
        int cuposCarros = Math.max(0, parqueadero.cuposDisponibles - cuposMotos);
        if (tipo == TipoVehiculo.MOTO) {
            cuposMotos = Math.min(capacidadMotos, cuposMotos + 1);
        } else {
            cuposCarros = Math.min(capacidadCarros, cuposCarros + 1);
        }
        parqueadero.cuposMotos = cuposMotos;
        parqueadero.cuposDisponibles = cuposCarros + cuposMotos;
    }

    private Estadia aDominio(EstadiaEntity entidad) {
        return Estadia
                .builder()
                .id(entidad.id)
                .parqueaderoId(entidad.parqueaderoId)
                .reservaId(entidad.reservaId)
                .placa(entidad.placa)
                .tipoVehiculo(entidad.tipoVehiculo)
                .ingresoEn(entidad.ingresoEn)
                .salidaEn(entidad.salidaEn)
                .minutos(entidad.minutos)
                .valorCobrado(entidad.valorCobrado)
                .build();
    }
}
