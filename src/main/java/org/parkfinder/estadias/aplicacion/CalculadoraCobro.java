package org.parkfinder.estadias.aplicacion;

import org.parkfinder.estadias.dominio.modelo.Cobro;
import org.parkfinder.estadias.dominio.modelo.Tarifas;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Calcula cuánto debe pagar un vehículo por el tiempo que estuvo en el parqueadero.
 *
 * Regla (minuto exacto con topes):
 * <ol>
 *   <li>Cada minuto vale tarifaHora / 60. El minuto iniciado se cobra completo, así que
 *       quien está menos de una hora paga exactamente sus minutos.</li>
 *   <li>Los minutos seguidos dentro de la franja nocturna (por defecto 18:00 a 06:00) nunca
 *       cuestan más que la tarifa noche: se cobra lo que sea menor.</li>
 *   <li>Cada bloque de 24 horas, contado desde el ingreso, nunca cuesta más que la tarifa día:
 *       se cobra lo que sea menor.</li>
 * </ol>
 * Si el parqueadero no tiene tarifa día o noche, ese tope no se aplica.
 * No depende de Quarkus ni de la base de datos.
 */
public class CalculadoraCobro {

    static final int MINUTOS_DIA = 24 * 60;

    private final LocalTime nocheInicio;
    private final LocalTime nocheFin;

    public CalculadoraCobro(LocalTime nocheInicio, LocalTime nocheFin) {
        this.nocheInicio = nocheInicio;
        this.nocheFin = nocheFin;
    }

    public Cobro calcular(Tarifas tarifas, LocalDateTime ingreso, LocalDateTime salida) {
        if (tarifas == null || tarifas.hora() == null) {
            throw new IllegalArgumentException("El parqueadero no tiene tarifa por hora configurada");
        }
        long segundos = Math.max(0, Duration.between(ingreso, salida).getSeconds());
        long minutos = (segundos + 59) / 60;
        double valorMinuto = tarifas.hora() / 60.0;

        double total = 0;
        long minutosDiurnos = 0;
        long minutosNocturnos = 0;
        boolean aplicoDia = false;
        boolean aplicoNoche = false;

        for (long inicioBloque = 0; inicioBloque < minutos; inicioBloque += MINUTOS_DIA) {
            long finBloque = Math.min(minutos, inicioBloque + MINUTOS_DIA);
            long diurnosBloque = 0;
            double costoNoches = 0;
            long nocheSeguida = 0;

            for (long m = inicioBloque; m < finBloque; m++) {
                if (esNoche(ingreso.plusMinutes(m).toLocalTime())) {
                    nocheSeguida++;
                } else {
                    if (nocheSeguida > 0) {
                        double noche = costoNoche(nocheSeguida, valorMinuto, tarifas.noche());
                        aplicoNoche |= noche < nocheSeguida * valorMinuto;
                        costoNoches += noche;
                        minutosNocturnos += nocheSeguida;
                        nocheSeguida = 0;
                    }
                    diurnosBloque++;
                }
            }
            if (nocheSeguida > 0) {
                double noche = costoNoche(nocheSeguida, valorMinuto, tarifas.noche());
                aplicoNoche |= noche < nocheSeguida * valorMinuto;
                costoNoches += noche;
                minutosNocturnos += nocheSeguida;
            }
            minutosDiurnos += diurnosBloque;

            double costoBloque = diurnosBloque * valorMinuto + costoNoches;
            if (tarifas.dia() != null && tarifas.dia() < costoBloque) {
                costoBloque = tarifas.dia();
                aplicoDia = true;
            }
            total += costoBloque;
        }

        return new Cobro(minutos, minutos / MINUTOS_DIA, minutosDiurnos, minutosNocturnos,
                valorMinuto, Math.round(total), aplicoDia, aplicoNoche);
    }

    private static double costoNoche(long minutos, double valorMinuto, Double tarifaNoche) {
        double porMinuto = minutos * valorMinuto;
        return tarifaNoche == null ? porMinuto : Math.min(porMinuto, tarifaNoche);
    }

    /** La franja puede cruzar la medianoche (18:00 a 06:00) o no (por ejemplo 00:00 a 05:00). */
    boolean esNoche(LocalTime hora) {
        if (nocheInicio.isAfter(nocheFin)) {
            return !hora.isBefore(nocheInicio) || hora.isBefore(nocheFin);
        }
        return !hora.isBefore(nocheInicio) && hora.isBefore(nocheFin);
    }
}
