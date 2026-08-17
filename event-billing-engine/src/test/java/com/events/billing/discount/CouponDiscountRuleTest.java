package com.events.billing.discount;

import com.events.billing.exception.InvalidCouponException;
import com.events.billing.model.Customer;
import com.events.billing.model.CustomerCategory;
import com.events.billing.model.Event;
import com.events.billing.model.Purchase;
import com.events.billing.model.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CouponDiscountRule")
class CouponDiscountRuleTest {

    private static final double DELTA = 1e-9; // tolerancia para comparar doubles
    private static final LocalDate HOY = LocalDate.of(2026, 6, 1);

    /** Construye una compra con el codigo de cupon indicado (subtotal irrelevante aqui). */
    private Purchase compraConCupon(String codigoCupon) {
        Event evento = new Event("E1", "Concierto", LocalDate.of(2026, 12, 1), "concierto");
        Ticket ticket = new Ticket("T1", evento, 100.0);
        Customer cliente = new Customer("C1", "Ana", CustomerCategory.REGULAR);
        return new Purchase(cliente, List.of(ticket), codigoCupon, HOY);
    }

    @Test
    @DisplayName("deberia aplicar el descuento cuando el cupon es valido")
    void deberiaAplicarDescuentoCuandoCuponEsValido() {
        // Arrange
        Map<String, Coupon> repo = Map.of("PROMO20", new Coupon("PROMO20", 20.0, null));
        CouponDiscountRule regla = new CouponDiscountRule(repo);
        Purchase compra = compraConCupon("PROMO20");

        // Act
        double resultado = regla.apply(100.0, compra);

        // Assert
        assertEquals(80.0, resultado, DELTA);
    }

    @Test
    @DisplayName("deberia dejar el total en cero cuando el descuento supera el monto")
    void deberiaDejarTotalEnCeroCuandoDescuentoSuperaElMonto() {
        // Arrange
        Map<String, Coupon> repo = Map.of("PROMO50", new Coupon("PROMO50", 50.0, null));
        CouponDiscountRule regla = new CouponDiscountRule(repo);
        Purchase compra = compraConCupon("PROMO50");

        // Act
        double resultado = regla.apply(30.0, compra);

        // Assert -> nunca negativo
        assertEquals(0.0, resultado, DELTA);
    }

    @Test
    @DisplayName("deberia lanzar excepcion cuando el cupon no existe")
    void deberiaLanzarExcepcionCuandoCuponNoExiste() {
        // Arrange
        Map<String, Coupon> repo = Map.of("PROMO20", new Coupon("PROMO20", 20.0, null));
        CouponDiscountRule regla = new CouponDiscountRule(repo);
        Purchase compra = compraConCupon("NO_EXISTE");

        // Act + Assert
        assertThrows(InvalidCouponException.class, () -> regla.apply(100.0, compra));
    }

    @Test
    @DisplayName("deberia lanzar excepcion cuando el cupon esta expirado")
    void deberiaLanzarExcepcionCuandoCuponEstaExpirado() {
        // Arrange
        Coupon vencido = new Coupon("VIEJO", 20.0, LocalDate.of(2020, 1, 1));
        CouponDiscountRule regla = new CouponDiscountRule(Map.of("VIEJO", vencido));
        Purchase compra = compraConCupon("VIEJO");

        // Act + Assert
        assertThrows(InvalidCouponException.class, () -> regla.apply(100.0, compra));
    }

    @Test
    @DisplayName("deberia lanzar excepcion cuando el codigo de cupon es nulo")
    void deberiaLanzarExcepcionCuandoCodigoEsNulo() {
        // Arrange
        CouponDiscountRule regla = new CouponDiscountRule(Map.of("PROMO20", new Coupon("PROMO20", 20.0, null)));
        Purchase compra = compraConCupon(null);

        // Act + Assert
        assertThrows(InvalidCouponException.class, () -> regla.apply(100.0, compra));
    }

    @Test
    @DisplayName("deberia lanzar excepcion cuando el codigo de cupon es vacio")
    void deberiaLanzarExcepcionCuandoCodigoEsVacio() {
        // Arrange
        CouponDiscountRule regla = new CouponDiscountRule(Map.of("PROMO20", new Coupon("PROMO20", 20.0, null)));
        Purchase compra = compraConCupon("   ");

        // Act + Assert
        assertThrows(InvalidCouponException.class, () -> regla.apply(100.0, compra));
    }

    @ParameterizedTest(name = "monto={0}, cupon={1} -> esperado={2}")
    @CsvSource({
        "100.0, 20.0, 80.0",
        "50.0,  50.0, 0.0",
        "30.0,  40.0, 0.0",   // descuento mayor que el total -> 0
        "200.0, 0.0,  200.0"
    })
    @DisplayName("deberia calcular el total correcto para varias combinaciones monto/cupon")
    void deberiaCalcularTotalParaVariasCombinaciones(double monto, double montoCupon, double esperado) {
        // Arrange
        CouponDiscountRule regla = new CouponDiscountRule(Map.of("TEST", new Coupon("TEST", montoCupon, null)));
        Purchase compra = compraConCupon("TEST");

        // Act
        double resultado = regla.apply(monto, compra);

        // Assert
        assertEquals(esperado, resultado, DELTA);
    }
}
