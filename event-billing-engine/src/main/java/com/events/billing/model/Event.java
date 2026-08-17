package com.events.billing.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa un evento (concierto, conferencia, cine, etc.).
 * Clase inmutable: todos sus campos son final y se validan en el constructor.
 */
public final class Event {

    private final String id;
    private final String nombre;
    private final LocalDate fecha;
    private final String categoria; // ej. "concierto", "conferencia", "cine"

    public Event(String id, String nombre, LocalDate fecha, String categoria) {
        this.id = requireNonBlank(id, "id");
        this.nombre = requireNonBlank(nombre, "nombre");
        this.fecha = Objects.requireNonNull(fecha, "la fecha del evento no puede ser nula");
        this.categoria = requireNonBlank(categoria, "categoria");
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getCategoria() {
        return categoria;
    }

    private static String requireNonBlank(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede ser nulo o vacio");
        }
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Event{id='" + id + "', nombre='" + nombre + "', categoria='" + categoria + "'}";
    }
}
