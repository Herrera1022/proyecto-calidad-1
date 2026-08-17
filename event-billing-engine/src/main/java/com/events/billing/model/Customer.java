package com.events.billing.model;

/**
 * Representa al cliente que realiza la compra.
 * Clase inmutable.
 *
 * DECISION DE DISENO: se permite que la categoria sea null. La responsabilidad
 * de decidir que hacer con una categoria nula recae en la regla de descuento
 * (ver {@code CustomerCategoryDiscountRule}), que la trata como REGULAR.
 */
public final class Customer {

    private final String id;
    private final String nombre;
    private final CustomerCategory categoria; // puede ser null (ver nota arriba)

    public Customer(String id, String nombre, CustomerCategory categoria) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del cliente no puede ser nulo o vacio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente no puede ser nulo o vacio");
        }
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public CustomerCategory getCategoria() {
        return categoria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Customer{id='" + id + "', nombre='" + nombre + "', categoria=" + categoria + "}";
    }
}
