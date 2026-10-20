package com.events.billing.console;

import com.events.billing.model.Event;

/** Evento de la cartelera con su precio de entrada (tipo de la UI, no del dominio). */
record Espectaculo(Event evento, double precio) {

    String nombre() {
        return evento.getNombre();
    }
}
