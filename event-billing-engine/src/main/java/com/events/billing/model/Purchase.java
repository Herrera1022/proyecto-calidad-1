package com.events.billing.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Representa una compra: un cliente, la lista de tickets adquiridos, un codigo
 * de cupon OPCIONAL (puede ser null) y la fecha de compra.
 * Inmutable (record): la lista de tickets se copia defensivamente.
 *
 * @param cliente     quien compra (no nulo)
 * @param tickets     al menos un ticket
 * @param codigoCupon opcional: puede ser null (sin cupon)
 * @param fechaCompra fecha de la compra (no nula)
 */
public record Purchase(Customer cliente, List<Ticket> tickets, String codigoCupon, LocalDate fechaCompra) {

    public Purchase {
        Objects.requireNonNull(cliente, "el cliente no puede ser nulo");
        Objects.requireNonNull(tickets, "la lista de tickets no puede ser nula");
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("La compra debe tener al menos un ticket");
        }
        tickets = List.copyOf(tickets); // copia inmutable
        Objects.requireNonNull(fechaCompra, "la fecha de compra no puede ser nula");
    }

    /** @return la suma de los precios base de todos los tickets. */
    public double subtotal() {
        return tickets.stream()
                .mapToDouble(Ticket::getPrecioBase)
                .sum();
    }

    @Override
    public String toString() {
        return "Purchase{cliente=" + cliente.getId()
                + ", tickets=" + tickets.size()
                + ", codigoCupon='" + codigoCupon + "'"
                + ", subtotal=" + subtotal() + "}";
    }
}
