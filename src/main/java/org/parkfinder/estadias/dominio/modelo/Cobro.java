package org.parkfinder.estadias.dominio.modelo;

/**
 * Resultado del cálculo de lo que debe pagar un vehículo por el tiempo que estuvo.
 *
 * @param minutos           minutos cobrados (el minuto iniciado se cobra completo)
 * @param diasCompletos     bloques completos de 24 horas
 * @param minutosDiurnos    minutos fuera de la franja nocturna
 * @param minutosNocturnos  minutos dentro de la franja nocturna
 * @param valorMinuto       tarifa hora / 60
 * @param valorTotal        valor a pagar, redondeado al peso
 * @param aplicoTarifaDia   true si en algún bloque de 24 h se cobró la tarifa día por ser más barata
 * @param aplicoTarifaNoche true si en alguna noche se cobró la tarifa noche por ser más barata
 */
public record Cobro(
        long minutos,
        long diasCompletos,
        long minutosDiurnos,
        long minutosNocturnos,
        double valorMinuto,
        double valorTotal,
        boolean aplicoTarifaDia,
        boolean aplicoTarifaNoche
) {
}
