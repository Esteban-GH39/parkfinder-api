package org.parkfinder.estadias.dominio.modelo;

import java.time.LocalDateTime;

/**
 * Lo que debe pagar un vehículo hasta un momento dado ({@code corte}).
 *
 * @param cerrada true si ya se registró la salida y se liberó el cupo; false si es solo una consulta.
 */
public record Liquidacion(Estadia estadia, Tarifas tarifas, Cobro cobro, LocalDateTime corte, boolean cerrada) {
}
