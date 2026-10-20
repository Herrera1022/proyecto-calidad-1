package com.events.billing.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Customer")
class CustomerTest {

    @Test
    @DisplayName("deberia exponer los datos con que se construyo")
    void deberiaExponerLosDatosConQueSeConstruyo() {
        // Arrange + Act
        Customer cliente = new Customer("C1", "Ana", CustomerCategory.STUDENT);

        // Assert
        assertEquals("C1", cliente.getId());
        assertEquals("Ana", cliente.getNombre());
        assertEquals(CustomerCategory.STUDENT, cliente.getCategoria());
    }

    @Test
    @DisplayName("deberia permitir una categoria nula (se trata como REGULAR en la regla)")
    void deberiaPermitirCategoriaNula() {
        // Arrange + Act
        Customer cliente = new Customer("C1", "Ana", null);

        // Assert
        assertNull(cliente.getCategoria());
    }

    @ParameterizedTest(name = "id=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("deberia rechazar un id nulo o vacio")
    void deberiaRechazarIdNuloOVacio(String id) {
        assertThrows(IllegalArgumentException.class, () -> new Customer(id, "Ana", CustomerCategory.REGULAR));
    }

    @ParameterizedTest(name = "nombre=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("deberia rechazar un nombre nulo o vacio")
    void deberiaRechazarNombreNuloOVacio(String nombre) {
        assertThrows(IllegalArgumentException.class, () -> new Customer("C1", nombre, CustomerCategory.REGULAR));
    }

    @Test
    @DisplayName("deberia considerar iguales dos clientes con el mismo id")
    void deberiaConsiderarIgualesClientesConMismoId() {
        // Arrange
        Customer a = new Customer("C1", "Ana", CustomerCategory.STUDENT);
        Customer b = new Customer("C1", "Ana Maria", CustomerCategory.SENIOR);

        // Act + Assert
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("deberia considerar distintos clientes con id diferente, nulos u objetos de otro tipo")
    void deberiaConsiderarDistintosClientesConIdDiferente() {
        // Arrange
        Customer a = new Customer("C1", "Ana", CustomerCategory.STUDENT);
        Customer b = new Customer("C2", "Ana", CustomerCategory.STUDENT);

        // Act + Assert
        assertNotEquals(a, b);
        assertNotEquals(null, a);
        assertNotEquals(a, new Object());
    }

    @Test
    @DisplayName("deberia describir el cliente en toString")
    void deberiaDescribirElClienteEnToString() {
        // Arrange
        Customer cliente = new Customer("C1", "Ana", CustomerCategory.SENIOR);

        // Act
        String texto = cliente.toString();

        // Assert
        assertTrue(texto.contains("C1"));
        assertTrue(texto.contains("Ana"));
        assertTrue(texto.contains("SENIOR"));
    }
}
