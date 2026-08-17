package com.events.billing.model;

import java.util.Objects;

/**
 * Representa un boleto para un evento, con su precio base.
 * Clase inmutable con validacion en el constructor.
 */
public final class Ticket {

    private final String id;
    private final Event evento;
    private final double precioBase;

    public Ticket(String id, Event evento, double precioBase) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del ticket no puede ser nulo o vacio");
        }
        this.id = id;
        this.evento = Objects.requireNonNull(evento, "el evento del ticket no puede ser nulo");
        if (precioBase < 0) {
            throw new IllegalArgumentException("El precio base no puede ser negativo: " + precioBase);
        }
        this.precioBase = precioBase;
    }

    public String getId() {
        return id;
    }

    public Event getEvento() {
        return evento;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticket other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Ticket{id='" + id + "', precioBase=" + precioBase + "}";
    }
}
