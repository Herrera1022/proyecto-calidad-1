package com.events.billing.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Event")
class EventTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 12, 1);

    @Test
    @DisplayName("deberia exponer los datos con que se construyo")
    void deberiaExponerLosDatosConQueSeConstruyo() {
        // Arrange + Act
        Event evento = new Event("E1", "Concierto Rock", FECHA, "concierto");

        // Assert
        assertEquals("E1", evento.getId());
        assertEquals("Concierto Rock", evento.getNombre());
        assertEquals(FECHA, evento.getFecha());
        assertEquals("concierto", evento.getCategoria());
    }

    @ParameterizedTest(name = "id=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("deberia rechazar un id nulo o vacio")
    void deberiaRechazarIdNuloOVacio(String id) {
        assertThrows(IllegalArgumentException.class, () -> new Event(id, "Concierto", FECHA, "concierto"));
    }

    @ParameterizedTest(name = "nombre=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("deberia rechazar un nombre nulo o vacio")
    void deberiaRechazarNombreNuloOVacio(String nombre) {
        assertThrows(IllegalArgumentException.class, () -> new Event("E1", nombre, FECHA, "concierto"));
    }

    @ParameterizedTest(name = "categoria=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("deberia rechazar una categoria nula o vacia")
    void deberiaRechazarCategoriaNulaOVacia(String categoria) {
        assertThrows(IllegalArgumentException.class, () -> new Event("E1", "Concierto", FECHA, categoria));
    }

    @Test
    @DisplayName("deberia rechazar una fecha nula")
    void deberiaRechazarFechaNula() {
        assertThrows(NullPointerException.class, () -> new Event("E1", "Concierto", null, "concierto"));
    }

    @Test
    @DisplayName("deberia considerar iguales dos eventos con el mismo id")
    void deberiaConsiderarIgualesEventosConMismoId() {
        // Arrange
        Event a = new Event("E1", "Concierto", FECHA, "concierto");
        Event b = new Event("E1", "Otro nombre", FECHA.plusDays(3), "cine");

        // Act + Assert
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("deberia considerar distintos eventos con id diferente, nulos u objetos de otro tipo")
    void deberiaConsiderarDistintosEventosConIdDiferente() {
        // Arrange
        Event a = new Event("E1", "Concierto", FECHA, "concierto");
        Event b = new Event("E2", "Concierto", FECHA, "concierto");

        // Act + Assert
        assertNotEquals(a, b);
        assertNotEquals(null, a);
        assertNotEquals(a, new Object());
    }

    @Test
    @DisplayName("deberia describir el evento en toString")
    void deberiaDescribirElEventoEnToString() {
        // Arrange
        Event evento = new Event("E1", "Concierto Rock", FECHA, "concierto");

        // Act
        String texto = evento.toString();

        // Assert
        assertTrue(texto.contains("E1"));
        assertTrue(texto.contains("Concierto Rock"));
        assertTrue(texto.contains("concierto"));
    }
}
