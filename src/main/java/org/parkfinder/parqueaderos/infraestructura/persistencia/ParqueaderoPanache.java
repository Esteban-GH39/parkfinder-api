package org.parkfinder.parqueaderos.infraestructura.persistencia;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.parkfinder.parqueaderos.dominio.excepcion.ParqueaderoNoEncontradoException;
import org.parkfinder.parqueaderos.dominio.modelo.HistorialCambio;
import org.parkfinder.parqueaderos.dominio.modelo.HistorialCambioEntity;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoActualizacion;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoConsulta;
import org.parkfinder.parqueaderos.dominio.modelo.ParqueaderoEntity;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@ApplicationScoped
public class ParqueaderoPanache implements ParqueaderoRepositorio, PanacheRepository<ParqueaderoEntity> {
    //MÉTODO PARA CREAR PARQUEADERO
    @Override
    @Transactional
    public void crearPark(Parqueadero parqueadero){
        ParqueaderoEntity parqueaderoEntity = ParqueaderoEntity
                .builder()
                .nombre(parqueadero.nombre)
                .direccion(parqueadero.direccion)
                .zona(parqueadero.zona)
                .capacidadTotal(parqueadero.capacidadTotal)
                .tarifaHora(parqueadero.tarifaHora)
                .tarifaDia(parqueadero.tarifaDia)
                .tarifaNoche(parqueadero.tarifaNoche)
                .horaInicio(parqueadero.horaInicio)
                .horaFin(parqueadero.horaFin)
                .nombrePropietario(parqueadero.nombrePropietario)
                .build();
        persist(parqueaderoEntity);
    }

    //MÉTODO PARA EDITAR PARQUEADERO (HU-19)
    @Override
    @Transactional
    public List<HistorialCambio> actualizarPark(Long id, ParqueaderoActualizacion datos) {
        ParqueaderoEntity entidad = findById(id);
        if (entidad == null) {
            throw new ParqueaderoNoEncontradoException(id);
        }

        List<HistorialCambio> cambios = new ArrayList<>();
        registrar(cambios, id, "nombre", entidad.nombre, datos.nombre,
                valor -> entidad.nombre = valor);
        registrar(cambios, id, "direccion", entidad.direccion, datos.direccion,
                valor -> entidad.direccion = valor);
        registrar(cambios, id, "zona", entidad.zona, datos.zona,
                valor -> entidad.zona = valor);
        registrar(cambios, id, "capacidadTotal", entidad.capacidadTotal, datos.capacidadTotal,
                valor -> entidad.capacidadTotal = valor);
        registrar(cambios, id, "tarifaHora", entidad.tarifaHora, datos.tarifaHora,
                valor -> entidad.tarifaHora = valor);
        registrar(cambios, id, "tarifaDia", entidad.tarifaDia, datos.tarifaDia,
                valor -> entidad.tarifaDia = valor);
        registrar(cambios, id, "tarifaNoche", entidad.tarifaNoche, datos.tarifaNoche,
                valor -> entidad.tarifaNoche = valor);
        registrar(cambios, id, "horaInicio", entidad.horaInicio, datos.horaInicio,
                valor -> entidad.horaInicio = valor);
        registrar(cambios, id, "horaFin", entidad.horaFin, datos.horaFin,
                valor -> entidad.horaFin = valor);
        registrar(cambios, id, "nombrePropietario", entidad.nombrePropietario, datos.nombrePropietario,
                valor -> entidad.nombrePropietario = valor);

        // La entidad está gestionada dentro de la transacción, así que los
        // cambios se guardan solos. Falta persistir los renglones del historial.
        cambios.forEach(cambio -> HistorialCambioEntity
                .builder()
                .parqueaderoId(cambio.parqueaderoId)
                .campo(cambio.campo)
                .valorAnterior(cambio.valorAnterior)
                .valorNuevo(cambio.valorNuevo)
                .fecha(cambio.fecha)
                .build()
                .persist());

        return cambios;
    }

    //MÉTODO PARA CONSULTAR EL HISTORIAL DE CAMBIOS (HU-19)
    @Override
    public List<HistorialCambio> historial(Long id) {
        if (findById(id) == null) {
            throw new ParqueaderoNoEncontradoException(id);
        }
        List<HistorialCambioEntity> entidades =
                HistorialCambioEntity.list("parqueaderoId = ?1 order by fecha desc", id);

        return entidades.stream()
                .map(entidad -> HistorialCambio
                        .builder()
                        .parqueaderoId(entidad.parqueaderoId)
                        .campo(entidad.campo)
                        .valorAnterior(entidad.valorAnterior)
                        .valorNuevo(entidad.valorNuevo)
                        .fecha(entidad.fecha)
                        .build())
                .toList();
    }

    //MÉTODO PARA CONSULTAR UN PARQUEADERO (HU-19)
    @Override
    public ParqueaderoConsulta obtenerPorId(Long id) {
        ParqueaderoEntity entidad = findById(id);
        if (entidad == null) {
            throw new ParqueaderoNoEncontradoException(id);
        }
        return aConsulta(entidad);
    }

    //MÉTODO PARA LISTAR PARQUEADEROS (HU-19)
    @Override
    public List<ParqueaderoConsulta> listar() {
        return listAll().stream().map(this::aConsulta).toList();
    }

    private ParqueaderoConsulta aConsulta(ParqueaderoEntity entidad) {
        return ParqueaderoConsulta
                .builder()
                .id(entidad.id)
                .nombre(entidad.nombre)
                .direccion(entidad.direccion)
                .zona(entidad.zona)
                .capacidadTotal(entidad.capacidadTotal)
                .tarifaHora(entidad.tarifaHora)
                .tarifaDia(entidad.tarifaDia)
                .tarifaNoche(entidad.tarifaNoche)
                .horaInicio(entidad.horaInicio)
                .horaFin(entidad.horaFin)
                .nombrePropietario(entidad.nombrePropietario)
                .build();
    }

    /**
     * Aplica el valor nuevo y lo anota en el historial solo si llegó y es
     * distinto del actual, para no llenar la trazabilidad de renglones que no
     * representan un cambio real.
     */
    private <T> void registrar(List<HistorialCambio> cambios, Long parqueaderoId, String campo,
                               T actual, T nuevo, Consumer<T> asignar) {
        if (nuevo == null || Objects.equals(actual, nuevo)) {
            return;
        }
        cambios.add(HistorialCambio
                .builder()
                .parqueaderoId(parqueaderoId)
                .campo(campo)
                .valorAnterior(actual == null ? null : actual.toString())
                .valorNuevo(nuevo.toString())
                .fecha(LocalDateTime.now())
                .build());
        asignar.accept(nuevo);
    }

}
