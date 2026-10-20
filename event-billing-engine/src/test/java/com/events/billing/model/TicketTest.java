package com.events.billing.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Ticket")
class TicketTest {

    private static final double DELTA = 1e-9;
    private final Event evento = new Event("E1", "Concierto", LocalDate.of(2026, 12, 1), "concierto");

    @Test
    @DisplayName("deberia exponer los datos con que se construyo")
    void deberiaExponerLosDatosConQueSeConstruyo() {
        // Arrange + Act
        Ticket ticket = new Ticket("T1", evento, 120.0);

        // Assert
        assertEquals("T1", ticket.getId());
        assertSame(evento, ticket.getEvento());
        assertEquals(120.0, ticket.getPrecioBase(), DELTA);
    }

    @Test
    @DisplayName("deberia permitir un precio base de cero (entrada de cortesia)")
    void deberiaPermitirPrecioBaseCero() {
        // Arrange + Act
        Ticket ticket = new Ticket("T1", evento, 0.0);

        // Assert
        assertEquals(0.0, ticket.getPrecioBase(), DELTA);
    }

    @ParameterizedTest(name = "id=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("deberia rechazar un id nulo o vacio")
    void deberiaRechazarIdNuloOVacio(String id) {
        assertThrows(IllegalArgumentException.class, () -> new Ticket(id, evento, 10.0));
    }

    @Test
    @DisplayName("deberia rechazar un evento nulo")
    void deberiaRechazarEventoNulo() {
        assertThrows(NullPointerException.class, () -> new Ticket("T1", null, 10.0));
    }

    @Test
    @DisplayName("deberia rechazar un precio base negativo")
    void deberiaRechazarPrecioBaseNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new Ticket("T1", evento, -0.01));
    }

    @Test
    @DisplayName("deberia considerar iguales dos tickets con el mismo id")
    void deberiaConsiderarIgualesTicketsConMismoId() {
        // Arrange
        Ticket a = new Ticket("T1", evento, 10.0);
        Ticket b = new Ticket("T1", evento, 99.0);

        // Act + Assert
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("deberia considerar distintos tickets con id diferente, nulos u objetos de otro tipo")
    void deberiaConsiderarDistintosTicketsConIdDiferente() {
        // Arrange
        Ticket a = new Ticket("T1", evento, 10.0);
        Ticket b = new Ticket("T2", evento, 10.0);

        // Act + Assert
        assertNotEquals(a, b);
        assertNotEquals(null, a);
        assertNotEquals(a, new Object());
    }

    @Test
    @DisplayName("deberia describir el ticket en toString")
    void deberiaDescribirElTicketEnToString() {
        // Arrange
        Ticket ticket = new Ticket("T1", evento, 120.0);

        // Act
        String texto = ticket.toString();

        // Assert
        assertTrue(texto.contains("T1"));
        assertTrue(texto.contains("120.0"));
    }
}
