package com.events.billing.discount;

import com.events.billing.model.Customer;
import com.events.billing.model.CustomerCategory;
import com.events.billing.model.Event;
import com.events.billing.model.Purchase;
import com.events.billing.model.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CustomerCategoryDiscountRule")
class CustomerCategoryDiscountRuleTest {

    private static final double DELTA = 1e-9;

    private final CustomerCategoryDiscountRule regla = new CustomerCategoryDiscountRule();

    /** Construye una compra cuyo cliente tiene la categoria indicada (puede ser null). */
    private Purchase compraConCategoria(CustomerCategory categoria) {
        Event evento = new Event("E1", "Concierto", LocalDate.of(2026, 12, 1), "concierto");
        Ticket ticket = new Ticket("T1", evento, 100.0);
        Customer cliente = new Customer("C1", "Ana", categoria);
        return new Purchase(cliente, List.of(ticket), null, LocalDate.of(2026, 6, 1));
    }

    @Test
    @DisplayName("deberia aplicar 15% de descuento para un estudiante")
    void deberiaAplicarDescuentoDelQuincePorcientoParaEstudiante() {
        // Arrange
        Purchase compra = compraConCategoria(CustomerCategory.STUDENT);

        // Act
        double resultado = regla.apply(100.0, compra);

        // Assert -> 100 - 15% = 85
        assertEquals(85.0, resultado, DELTA);
    }

    @Test
    @DisplayName("deberia aplicar 20% de descuento para un cliente senior")
    void deberiaAplicarDescuentoDelVeintePorcientoParaSenior() {
        // Arrange
        Purchase compra = compraConCategoria(CustomerCategory.SENIOR);

        // Act
        double resultado = regla.apply(100.0, compra);

        // Assert -> 100 - 20% = 80
        assertEquals(80.0, resultado, DELTA);
    }

    @Test
    @DisplayName("deberia no aplicar descuento para un cliente regular")
    void deberiaNoAplicarDescuentoParaRegular() {
        // Arrange
        Purchase compra = compraConCategoria(CustomerCategory.REGULAR);

        // Act
        double resultado = regla.apply(100.0, compra);

        // Assert -> monto sin cambios
        assertEquals(100.0, resultado, DELTA);
    }

    @Test
    @DisplayName("deberia tratar la categoria nula como REGULAR (sin descuento)")
    void deberiaTratarCategoriaNulaComoRegular() {
        // Arrange -> cliente con categoria null (decision documentada en la regla)
        Purchase compra = compraConCategoria(null);

        // Act
        double resultado = regla.apply(100.0, compra);

        // Assert -> se comporta como REGULAR: sin descuento
        assertEquals(100.0, resultado, DELTA);
    }

    @Test
    @DisplayName("deberia manejar montos pequenios sin errores ni negativos")
    void deberiaManejarMontosPequeniosSinError() {
        // Arrange
        Purchase compra = compraConCategoria(CustomerCategory.STUDENT);

        // Act -> 1.0 - 15% = 0.85
        double resultado = regla.apply(1.0, compra);

        // Assert
        assertEquals(0.85, resultado, DELTA);
    }
}
