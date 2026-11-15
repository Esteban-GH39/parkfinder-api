package org.parkfinder.estadias.aplicacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.parkfinder.estadias.dominio.excepcion.EstadiaException;
import org.parkfinder.estadias.dominio.modelo.Cobro;
import org.parkfinder.estadias.dominio.modelo.Estadia;
import org.parkfinder.estadias.dominio.modelo.Liquidacion;
import org.parkfinder.estadias.dominio.modelo.Tarifas;
import org.parkfinder.estadias.dominio.repositorio.EstadiaRepositorio;
import org.parkfinder.parqueaderos.dominio.excepcion.ParqueaderoNoEncontradoException;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;
import org.parkfinder.reservas.dominio.modelo.Reserva;
import org.parkfinder.reservas.dominio.modelo.TipoVehiculo;
import org.parkfinder.reservas.dominio.repositorio.ReservaRepositorio;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Control del tiempo de los vehículos en el parqueadero: el administrador registra la placa
 * al ingresar (directo o con código de reserva) y al liberar el cupo el sistema calcula el cobro.
 */
@ApplicationScoped
public class EstadiaServicio {

    private static final Pattern PLACA = Pattern.compile("^[A-Z0-9]{5,7}$");

    @Inject
    EstadiaRepositorio repositorio;

    @Inject
    ParqueaderoRepositorio parqueaderoRepositorio;

    @Inject
    ReservaRepositorio reservaRepositorio;

    /** Inicio de la franja nocturna (tarifa noche). */
    @ConfigProperty(name = "parkfinder.cobro.noche-inicio", defaultValue = "18:00")
    String nocheInicio = "18:00";

    /** Fin de la franja nocturna (tarifa noche). */
    @ConfigProperty(name = "parkfinder.cobro.noche-fin", defaultValue = "06:00")
    String nocheFin = "06:00";

    /** Hora actual; las pruebas la reemplazan para fijar el tiempo. */
    Supplier<LocalDateTime> reloj = LocalDateTime::now;

    //MÉTODO REGISTRAR INGRESO DIRECTO: se descuenta un cupo del tipo y se guarda la hora de ingreso
    @Transactional
    public Estadia ingresoDirecto(Long parqueaderoId, int adminId, String placa, TipoVehiculo tipo) {
        String placaNormalizada = normalizarPlaca(placa);
        if (tipo == null) {
            throw EstadiaException.invalida("Selecciona si el vehículo es carro o moto");
        }
        validarDueno(parqueaderoId, adminId);
        verificarQueNoEsteAdentro(placaNormalizada);

        Estadia estadia = Estadia
                .builder()
                .parqueaderoId(parqueaderoId)
                .placa(placaNormalizada)
                .tipoVehiculo(tipo)
                .ingresoEn(reloj.get())
                .build();
        return repositorio.registrarIngreso(estadia, true);
    }

    //MÉTODO REGISTRAR INGRESO CON CÓDIGO DE RESERVA: valida el código y guarda la placa de cada vehículo.
    //El cupo ya se descontó al reservar, así que no se descuenta de nuevo.
    @Transactional
    public List<Estadia> ingresoConReserva(Long reservaId, Long parqueaderoId, int adminId,
                                           List<VehiculoIngreso> vehiculos) {
        if (vehiculos == null || vehiculos.isEmpty()) {
            throw EstadiaException.invalida("Digita la placa de al menos un vehículo");
        }
        validarDueno(parqueaderoId, adminId);

        List<VehiculoIngreso> normalizados = new ArrayList<>();
        Set<String> placas = new HashSet<>();
        for (VehiculoIngreso v : vehiculos) {
            if (v.tipo() == null) {
                throw EstadiaException.invalida("Selecciona si el vehículo es carro o moto");
            }
            String placa = normalizarPlaca(v.placa());
            if (!placas.add(placa)) {
                throw EstadiaException.invalida("La placa " + placa + " está repetida");
            }
            normalizados.add(new VehiculoIngreso(placa, v.tipo()));
        }

        Reserva reserva = reservaRepositorio.obtenerPorId(reservaId);
        if (!parqueaderoId.equals(reserva.parqueaderoId)) {
            throw EstadiaException.invalida("La reserva " + reservaId + " no es de este parqueadero");
        }
        int reservadas = reserva.cantidad == null ? 1 : reserva.cantidad;
        int motosReservadas = reserva.cantidadMotos == null ? 0 : reserva.cantidadMotos;
        int carrosReservados = reservadas - motosReservadas;
        long motos = normalizados.stream().filter(v -> v.tipo() == TipoVehiculo.MOTO).count();
        long carros = normalizados.size() - motos;
        if (carros > carrosReservados) {
            throw EstadiaException.invalida("La reserva es de " + carrosReservados + " carro(s) y digitaste "
                    + carros + " placa(s) de carro");
        }
        if (motos > motosReservadas) {
            throw EstadiaException.invalida("La reserva es de " + motosReservadas + " moto(s) y digitaste "
                    + motos + " placa(s) de moto");
        }
        normalizados.forEach(v -> verificarQueNoEsteAdentro(v.placa()));

        // Lanza ReservaEstadoInvalidoException (409) si no está pagada o el código ya se usó
        reservaRepositorio.validar(reservaId);

        LocalDateTime ahora = reloj.get();
        return normalizados.stream()
                .map(v -> repositorio.registrarIngreso(Estadia
                        .builder()
                        .parqueaderoId(parqueaderoId)
                        .reservaId(reservaId)
                        .placa(v.placa())
                        .tipoVehiculo(v.tipo())
                        .ingresoEn(ahora)
                        .build(), false))
                .toList();
    }

    //MÉTODO CONSULTAR CUÁNTO DEBE PAGAR UN VEHÍCULO HASTA AHORA (no libera el cupo)
    public Liquidacion consultarCobro(Long parqueaderoId, int adminId, String placa) {
        Parqueadero parqueadero = validarDueno(parqueaderoId, adminId);
        Estadia estadia = buscarAdentro(parqueaderoId, normalizarPlaca(placa));
        LocalDateTime ahora = reloj.get();
        return new Liquidacion(estadia, tarifasDe(parqueadero, estadia.tipoVehiculo),
                cobrar(parqueadero, estadia, ahora), ahora, false);
    }

    //MÉTODO REGISTRAR SALIDA: calcula el cobro con la hora actual, la guarda y libera el cupo
    @Transactional
    public Liquidacion registrarSalida(Long parqueaderoId, int adminId, String placa) {
        Parqueadero parqueadero = validarDueno(parqueaderoId, adminId);
        Estadia estadia = buscarAdentro(parqueaderoId, normalizarPlaca(placa));
        LocalDateTime ahora = reloj.get();
        Cobro cobro = cobrar(parqueadero, estadia, ahora);
        Estadia cerrada = repositorio.registrarSalida(estadia.id, ahora, cobro.minutos(), cobro.valorTotal());
        return new Liquidacion(cerrada, tarifasDe(parqueadero, estadia.tipoVehiculo), cobro, ahora, true);
    }

    //MÉTODO LISTAR LOS VEHÍCULOS DENTRO DEL PARQUEADERO con lo que llevan acumulado
    public List<Liquidacion> listarActivas(Long parqueaderoId, int adminId) {
        Parqueadero parqueadero = validarDueno(parqueaderoId, adminId);
        LocalDateTime ahora = reloj.get();
        return repositorio.listarActivas(parqueaderoId).stream()
                .map(e -> new Liquidacion(e, tarifasDe(parqueadero, e.tipoVehiculo),
                        cobrar(parqueadero, e, ahora), ahora, false))
                .toList();
    }

    /** Tarifas del tipo de vehículo. La moto usa las del carro en las que no tenga una propia. */
    static Tarifas tarifasDe(Parqueadero p, TipoVehiculo tipo) {
        if (tipo == TipoVehiculo.MOTO) {
            return new Tarifas(
                    p.tarifaHoraMoto != null ? p.tarifaHoraMoto : p.tarifaHora,
                    p.tarifaDiaMoto != null ? p.tarifaDiaMoto : p.tarifaDia,
                    p.tarifaNocheMoto != null ? p.tarifaNocheMoto : p.tarifaNoche);
        }
        return new Tarifas(p.tarifaHora, p.tarifaDia, p.tarifaNoche);
    }

    /** Mayúsculas y sin espacios ni guiones: "abc-123" y "ABC 123" son la misma placa. */
    static String normalizarPlaca(String placa) {
        String limpia = placa == null ? "" : placa.replaceAll("[\\s-]", "").toUpperCase();
        if (!PLACA.matcher(limpia).matches()) {
            throw EstadiaException.invalida("La placa debe tener entre 5 y 7 letras o números (ej. ABC123)");
        }
        return limpia;
    }

    private Cobro cobrar(Parqueadero parqueadero, Estadia estadia, LocalDateTime hasta) {
        Tarifas tarifas = tarifasDe(parqueadero, estadia.tipoVehiculo);
        if (tarifas.hora() == null) {
            throw EstadiaException.invalida("El parqueadero no tiene tarifa por hora configurada");
        }
        CalculadoraCobro calculadora = new CalculadoraCobro(LocalTime.parse(nocheInicio), LocalTime.parse(nocheFin));
        return calculadora.calcular(tarifas, estadia.ingresoEn, hasta);
    }

    private Parqueadero validarDueno(Long parqueaderoId, int adminId) {
        Parqueadero parqueadero = parqueaderoRepositorio.buscarPorId(parqueaderoId)
                .orElseThrow(() -> new ParqueaderoNoEncontradoException(parqueaderoId));
        if (parqueadero.fk_parqueadero_admin != adminId) {
            throw EstadiaException.noPermitida("No tienes permisos sobre este parqueadero");
        }
        return parqueadero;
    }

    private void verificarQueNoEsteAdentro(String placa) {
        repositorio.buscarActivaPorPlaca(placa).ifPresent(e -> {
            throw EstadiaException.conflicto("El vehículo " + placa + " ya está registrado dentro de un "
                    + "parqueadero desde " + e.ingresoEn.withNano(0).toString().replace('T', ' '));
        });
    }

    private Estadia buscarAdentro(Long parqueaderoId, String placa) {
        return repositorio.buscarActivaPorPlaca(placa)
                .filter(e -> parqueaderoId.equals(e.parqueaderoId))
                .orElseThrow(() -> EstadiaException.noEncontrada(
                        "No hay un vehículo con placa " + placa + " dentro de este parqueadero"));
    }

    /** Placa y tipo de un vehículo que ingresa. */
    public record VehiculoIngreso(String placa, TipoVehiculo tipo) {
    }
}
