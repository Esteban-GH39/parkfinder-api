package org.parkfinder.pagos.dominio.modelo;

import java.util.List;

/** Medios de pago que acepta la pasarela simulada (HU-07) y los datos que pide cada uno. */
public enum MedioPago {
    TARJETA("Tarjeta", List.of("numeroTarjeta", "titular", "vencimiento", "cvv")),
    PSE("PSE", List.of("banco", "documento")),
    NEQUI("Nequi", List.of("celular")),
    /** Paga con el saldo de la billetera del cliente: no pasa por la pasarela ni pide datos. Solo sirve para pagar reservas, no para recargar. */
    SALDO("Saldo", List.of());

    public final String etiqueta;
    public final List<String> camposRequeridos;

    MedioPago(String etiqueta, List<String> camposRequeridos) {
        this.etiqueta = etiqueta;
        this.camposRequeridos = camposRequeridos;
    }
}
