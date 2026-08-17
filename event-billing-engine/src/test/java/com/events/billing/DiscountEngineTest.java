package com.events.billing;

import com.events.billing.discount.Coupon;
import com.events.billing.discount.CouponDiscountRule;
import com.events.billing.discount.CustomerCategoryDiscountRule;
import com.events.billing.discount.DiscountRule;
import com.events.billing.model.Customer;
import com.events.billing.model.CustomerCategory;
import com.events.billing.model.Event;
import com.events.billing.model.Purchase;
import com.events.billing.model.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DiscountEngine")
class DiscountEngineTest {

    private static final double DELTA = 1e-9;
    private static final LocalDate HOY = LocalDate.of(2026, 6, 1);

    private final Event evento = new Event("E1", "Concierto", LocalDate.of(2026, 12, 1), "concierto");

    /** Compra con subtotal 100 (2 tickets de 50), categoria y cupon configurables. */
    private Purchase compra(CustomerCategory categoria, String codigoCupon) {
        Ticket t1 = new Ticket("T1", evento, 50.0);
        Ticket t2 = new Ticket("T2", evento, 50.0);
        Customer cliente = new Customer("C1", "Ana", categoria);
        return new Purchase(cliente, List.of(t1, t2), codigoCupon, HOY);
    }

    private CouponDiscountRule reglaCupon(String codigo, double monto) {
        return new CouponDiscountRule(Map.of(codigo, new Coupon(codigo, monto, null)));
    }

    @Test
    @DisplayName("deberia aplicar solo el cupon cuando es la unica regla")
    void deberiaAplicarSoloCuponCuandoEsLaUnicaRegla() {
        // Arrange
        DiscountRule cupon = reglaCupon("PROMO20", 20.0);
        DiscountEngine motor = new DiscountEngine(List.of(cupon));
        Purchase compra = compra(CustomerCategory.REGULAR, "PROMO20");

        // Act
        double total = motor.calculateTotal(compra);

        // Assert -> 100 - 20 = 80
        assertEquals(80.0, total, DELTA);
    }

    @Test
    @DisplayName("deberia aplicar solo la categoria cuando es la unica regla")
    void deberiaAplicarSoloCategoriaCuandoEsLaUnicaRegla() {
        // Arrange
        DiscountRule categoria = new CustomerCategoryDiscountRule();
        DiscountEngine motor = new DiscountEngine(List.of(categoria));
        Purchase compra = compra(CustomerCategory.STUDENT, null);

        // Act
        double total = motor.calculateTotal(compra);

        // Assert -> 100 - 15% = 85
        assertEquals(85.0, total, DELTA);
    }

    @Test
    @DisplayName("deberia aplicar cupon y categoria en orden secuencial (categoria -> cupon)")
    void deberiaAplicarCuponYCategoriaEnOrdenSecuencial() {
        // Arrange -> orden de la lista: primero categoria (porcentaje), luego cupon (fijo)
        DiscountRule categoria = new CustomerCategoryDiscountRule();
        DiscountRule cupon = reglaCupon("PROMO20", 20.0);
        DiscountEngine motor = new DiscountEngine(List.of(categoria, cupon));
        Purchase compra = compra(CustomerCategory.STUDENT, "PROMO20");

        // Act
        double total = motor.calculateTotal(compra);

        // Assert -> 100 -> (15%) 85 -> (-20) 65
        assertEquals(65.0, total, DELTA);
    }

    @Test
    @DisplayName("deberia retornar el subtotal cuando no hay reglas")
    void deberiaRetornarSubtotalCuandoNoHayReglas() {
        // Arrange
        DiscountEngine motor = new DiscountEngine(List.of());
        Purchase compra = compra(CustomerCategory.STUDENT, null);

        // Act
        double total = motor.calculateTotal(compra);

        // Assert -> sin descuentos, total = subtotal
        assertEquals(100.0, total, DELTA);
    }

    @Test
    @DisplayName("deberia garantizar que el total nunca sea negativo al combinar reglas")
    void deberiaGarantizarQueTotalNuncaSeaNegativo() {
        // Arrange -> senior 20% (100->80) y cupon de 200 (80->0, no negativo)
        DiscountRule categoria = new CustomerCategoryDiscountRule();
        DiscountRule cupon = reglaCupon("MEGA", 200.0);
        DiscountEngine motor = new DiscountEngine(List.of(categoria, cupon));
        Purchase compra = compra(CustomerCategory.SENIOR, "MEGA");

        // Act
        double total = motor.calculateTotal(compra);

        // Assert
        assertEquals(0.0, total, DELTA);
        assertTrue(total >= 0.0, "el total nunca debe ser negativo");
    }
}
