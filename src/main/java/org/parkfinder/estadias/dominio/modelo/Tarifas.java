package org.parkfinder.estadias.dominio.modelo;

/**
 * Tarifas que se aplican a un vehículo según su tipo.
 *
 * @param hora  valor de la hora; el minuto vale hora / 60. Obligatoria.
 * @param dia   tope por cada bloque de 24 horas; null si el parqueadero no tiene tarifa día.
 * @param noche tope por cada noche (franja nocturna seguida); null si no tiene tarifa noche.
 */
public record Tarifas(Double hora, Double dia, Double noche) {
}
