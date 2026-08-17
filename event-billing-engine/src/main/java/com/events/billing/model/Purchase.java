package com.events.billing.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Representa una compra: un cliente, la lista de tickets adquiridos, un codigo
 * de cupon OPCIONAL (puede ser null) y la fecha de compra.
 * Clase inmutable: la lista de tickets se copia defensivamente.
 */
public final class Purchase {

    private final Customer cliente;
    private final List<Ticket> tickets;
    private final String codigoCupon;   // opcional: puede ser null
    private final LocalDate fechaCompra;

    public Purchase(Customer cliente, List<Ticket> tickets, String codigoCupon, LocalDate fechaCompra) {
        this.cliente = Objects.requireNonNull(cliente, "el cliente no puede ser nulo");
        Objects.requireNonNull(tickets, "la lista de tickets no puede ser nula");
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("La compra debe tener al menos un ticket");
        }
        this.tickets = List.copyOf(tickets); // copia inmutable
        this.codigoCupon = codigoCupon;       // se permite null (sin cupon)
        this.fechaCompra = Objects.requireNonNull(fechaCompra, "la fecha de compra no puede ser nula");
    }

    /** @return la suma de los precios base de todos los tickets. */
    public double getSubtotal() {
        return tickets.stream()
                .mapToDouble(Ticket::getPrecioBase)
                .sum();
    }

    public Customer getCliente() {
        return cliente;
    }

    public List<Ticket> getTickets() {
        return tickets; // ya es inmutable
    }

    public String getCodigoCupon() {
        return codigoCupon;
    }

    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    @Override
    public String toString() {
        return "Purchase{cliente=" + cliente.getId()
                + ", tickets=" + tickets.size()
                + ", codigoCupon='" + codigoCupon + "'"
                + ", subtotal=" + getSubtotal() + "}";
    }
}
