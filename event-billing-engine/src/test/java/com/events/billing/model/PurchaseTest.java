package com.events.billing.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Purchase")
class PurchaseTest {

    private static final double DELTA = 1e-9;
    private static final LocalDate HOY = LocalDate.of(2026, 6, 1);

    private final Event evento = new Event("E1", "Concierto", LocalDate.of(2026, 12, 1), "concierto");
    private final Customer cliente = new Customer("C1", "Ana", CustomerCategory.REGULAR);
    private final List<Ticket> tickets = List.of(new Ticket("T1", evento, 100.0), new Ticket("T2", evento, 50.0));

    @Test
    @DisplayName("deberia exponer los datos con que se construyo")
    void deberiaExponerLosDatosConQueSeConstruyo() {
        // Arrange + Act
        Purchase compra = new Purchase(cliente, tickets, "PROMO20", HOY);

        // Assert
        assertSame(cliente, compra.getCliente());
        assertEquals(tickets, compra.getTickets());
        assertEquals("PROMO20", compra.getCodigoCupon());
        assertEquals(HOY, compra.getFechaCompra());
    }

    @Test
    @DisplayName("deberia calcular el subtotal como la suma de los precios base")
    void deberiaCalcularSubtotalComoSumaDePrecios() {
        // Arrange
        Purchase compra = new Purchase(cliente, tickets, null, HOY);

        // Act
        double subtotal = compra.getSubtotal();

        // Assert
        assertEquals(150.0, subtotal, DELTA);
    }

    @Test
    @DisplayName("deberia permitir una compra sin cupon")
    void deberiaPermitirCompraSinCupon() {
        // Arrange + Act
        Purchase compra = new Purchase(cliente, tickets, null, HOY);

        // Assert
        assertNull(compra.getCodigoCupon());
    }

    @Test
    @DisplayName("deberia copiar la lista de tickets para que la compra sea inmutable")
    void deberiaCopiarLaListaDeTickets() {
        // Arrange
        List<Ticket> original = new ArrayList<>(tickets);
        Purchase compra = new Purchase(cliente, original, null, HOY);

        // Act -> se modifica la lista original despues de crear la compra
        original.clear();

        // Assert
        assertEquals(2, compra.getTickets().size());
        List<Ticket> expuestos = compra.getTickets();
        assertThrows(UnsupportedOperationException.class, expuestos::clear);
    }

    @Test
    @DisplayName("deberia rechazar un cliente nulo")
    void deberiaRechazarClienteNulo() {
        assertThrows(NullPointerException.class, () -> new Purchase(null, tickets, null, HOY));
    }

    @Test
    @DisplayName("deberia rechazar una lista de tickets nula")
    void deberiaRechazarListaDeTicketsNula() {
        assertThrows(NullPointerException.class, () -> new Purchase(cliente, null, null, HOY));
    }

    @Test
    @DisplayName("deberia rechazar una compra sin tickets")
    void deberiaRechazarCompraSinTickets() {
        List<Ticket> vacia = List.of();
        assertThrows(IllegalArgumentException.class, () -> new Purchase(cliente, vacia, null, HOY));
    }

    @Test
    @DisplayName("deberia rechazar una fecha de compra nula")
    void deberiaRechazarFechaDeCompraNula() {
        assertThrows(NullPointerException.class, () -> new Purchase(cliente, tickets, null, null));
    }

    @Test
    @DisplayName("deberia describir la compra en toString")
    void deberiaDescribirLaCompraEnToString() {
        // Arrange
        Purchase compra = new Purchase(cliente, tickets, "PROMO20", HOY);

        // Act
        String texto = compra.toString();

        // Assert
        assertTrue(texto.contains("C1"));
        assertTrue(texto.contains("tickets=2"));
        assertTrue(texto.contains("PROMO20"));
        assertTrue(texto.contains("150.0"));
    }
}
