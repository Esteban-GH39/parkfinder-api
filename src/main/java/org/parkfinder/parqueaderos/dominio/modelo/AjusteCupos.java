package org.parkfinder.parqueaderos.dominio.modelo;

/**
 * HU-19: cuando el dueño edita la capacidad, los cupos libres se mueven lo
 * mismo que la capacidad. Los vehículos que ya están adentro (ocupados) no
 * cambian: por eso la ocupación no se edita, se deduce de capacidad - disponibles.
 */
public final class AjusteCupos {

    private AjusteCupos() {
    }

    /**
     * Cupos libres después de pasar de {@code capacidadAntes} a {@code capacidadNueva}.
     * Ej.: capacidad 50 con 30 libres (20 ocupados) y se sube a 60 → 40 libres.
     * Nunca queda por debajo de 0 ni por encima de la capacidad nueva.
     */
    public static int disponibles(int disponiblesAntes, int capacidadAntes, int capacidadNueva) {
        int ajustados = disponiblesAntes + (capacidadNueva - capacidadAntes);
        return Math.max(0, Math.min(capacidadNueva, ajustados));
    }
}
