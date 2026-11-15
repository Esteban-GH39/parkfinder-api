package org.parkfinder.estadias.aplicacion;

import org.junit.jupiter.api.Test;
import org.parkfinder.estadias.dominio.modelo.Cobro;
import org.parkfinder.estadias.dominio.modelo.Tarifas;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cobro por tiempo: minuto exacto (tarifa hora / 60) con tope de tarifa noche por cada noche
 * y tope de tarifa día por cada bloque de 24 horas. Noche de 18:00 a 06:00.
 */
class CalculadoraCobroTest {

    private final CalculadoraCobro calculadora = new CalculadoraCobro(LocalTime.of(18, 0), LocalTime.of(6, 0));

    // Hora 3.000 (minuto 50), día 25.000, noche 15.000
    private final Tarifas carro = new Tarifas(3000.0, 25000.0, 15000.0);

    private LocalDateTime a(int dia, int hora, int minuto) {
        return LocalDateTime.of(2026, 11, dia, hora, minuto);
    }

    @Test
    void menosDeUnaHora_cobraElMinutoExacto() {
        Cobro cobro = calculadora.calcular(carro, a(15, 10, 0), a(15, 10, 45));

        assertEquals(45, cobro.minutos());
        assertEquals(50.0, cobro.valorMinuto());
        assertEquals(2250.0, cobro.valorTotal());
        assertEquals(45, cobro.minutosDiurnos());
        assertEquals(0, cobro.minutosNocturnos());
    }

    @Test
    void elMinutoIniciadoSeCobraCompleto() {
        Cobro cobro = calculadora.calcular(carro, a(15, 10, 0), a(15, 10, 0).plusSeconds(61));

        assertEquals(2, cobro.minutos());
        assertEquals(100.0, cobro.valorTotal());
    }

    @Test
    void masDeUnaHora_sigueCobrandoAlMinuto() {
        Cobro cobro = calculadora.calcular(carro, a(15, 9, 0), a(15, 10, 30));

        assertEquals(90, cobro.minutos());
        assertEquals(4500.0, cobro.valorTotal());
    }

    @Test
    void llegaALas6pm_yPocoTiempo_noAlcanzaElTopeDeNoche() {
        // Ejemplo: llega a las 6 pm y sale a las 8 pm: 120 minutos nocturnos a 50 = 6.000
        Cobro cobro = calculadora.calcular(carro, a(15, 18, 0), a(15, 20, 0));

        assertEquals(120, cobro.minutosNocturnos());
        assertEquals(6000.0, cobro.valorTotal());
        assertFalse(cobro.aplicoTarifaNoche());
    }

    @Test
    void todaLaNoche_cobraLaTarifaNoche() {
        Cobro cobro = calculadora.calcular(carro, a(15, 18, 0), a(16, 6, 0));

        assertEquals(720, cobro.minutosNocturnos());
        assertEquals(15000.0, cobro.valorTotal());
        assertTrue(cobro.aplicoTarifaNoche());
    }

    @Test
    void parteDeDiaYParteDeNoche_sumaAmbas() {
        // 17:00 a 07:00: 60 min de día (3.000) + noche topada (15.000) + 60 min de día (3.000)
        Cobro cobro = calculadora.calcular(carro, a(15, 17, 0), a(16, 7, 0));

        assertEquals(120, cobro.minutosDiurnos());
        assertEquals(720, cobro.minutosNocturnos());
        assertEquals(21000.0, cobro.valorTotal());
    }

    @Test
    void muchasHorasEnElDia_cobraLaTarifaDia() {
        // 08:00 a 18:00 = 600 min = 30.000 al minuto, pero el día cuesta 25.000
        Cobro cobro = calculadora.calcular(carro, a(15, 8, 0), a(15, 18, 0));

        assertEquals(25000.0, cobro.valorTotal());
        assertTrue(cobro.aplicoTarifaDia());
    }

    @Test
    void variosDias_cobraUnaTarifaDiaPorCadaBloqueDe24h() {
        // 2 días completos (50.000) + 30 minutos de día (1.500)
        Cobro cobro = calculadora.calcular(carro, a(15, 10, 0), a(17, 10, 30));

        assertEquals(2, cobro.diasCompletos());
        assertEquals(51500.0, cobro.valorTotal());
    }

    @Test
    void sinTarifaDiaNiNoche_todoAlMinuto() {
        Cobro cobro = calculadora.calcular(new Tarifas(3000.0, null, null), a(15, 18, 0), a(16, 6, 0));

        assertEquals(36000.0, cobro.valorTotal());
        assertFalse(cobro.aplicoTarifaDia());
        assertFalse(cobro.aplicoTarifaNoche());
    }

    @Test
    void salidaInmediata_noCobraNada() {
        Cobro cobro = calculadora.calcular(carro, a(15, 10, 0), a(15, 10, 0));

        assertEquals(0, cobro.minutos());
        assertEquals(0.0, cobro.valorTotal());
    }

    @Test
    void redondeaAlPeso() {
        // 1.000 / 60 = 16,67 por minuto; 7 minutos = 116,67 -> 117
        Cobro cobro = calculadora.calcular(new Tarifas(1000.0, null, null), a(15, 10, 0), a(15, 10, 7));

        assertEquals(117.0, cobro.valorTotal());
    }

    @Test
    void sinTarifaHora_seRechaza() {
        assertThrows(IllegalArgumentException.class, () ->
                calculadora.calcular(new Tarifas(null, 25000.0, 15000.0), a(15, 10, 0), a(15, 11, 0)));
    }
}
