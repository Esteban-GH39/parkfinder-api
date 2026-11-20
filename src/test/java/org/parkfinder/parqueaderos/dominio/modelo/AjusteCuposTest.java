package org.parkfinder.parqueaderos.dominio.modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** HU-19: los cupos libres siguen a la capacidad sin tocar los ocupados. */
class AjusteCuposTest {

    @Test
    void subirLaCapacidad_sumaCuposLibres() {
        // 50 de capacidad, 30 libres (20 ocupados) -> 60 de capacidad, 40 libres
        assertEquals(40, AjusteCupos.disponibles(30, 50, 60));
    }

    @Test
    void bajarLaCapacidad_restaCuposLibres() {
        assertEquals(20, AjusteCupos.disponibles(30, 50, 40));
    }

    @Test
    void sinCambioDeCapacidad_quedaIgual() {
        assertEquals(30, AjusteCupos.disponibles(30, 50, 50));
    }

    @Test
    void nuncaQuedaNegativo() {
        // 20 ocupados y la capacidad baja a 10: no quedan cupos libres
        assertEquals(0, AjusteCupos.disponibles(30, 50, 10));
    }

    @Test
    void nuncaSuperaLaCapacidad() {
        assertEquals(5, AjusteCupos.disponibles(8, 5, 5));
    }
}
