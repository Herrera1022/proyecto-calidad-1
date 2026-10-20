package com.events.billing.discount;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Coupon")
class CouponTest {

    private static final double DELTA = 1e-9;
    private static final LocalDate VENCE = LocalDate.of(2026, 6, 30);

    @Test
    @DisplayName("deberia exponer los datos con que se construyo")
    void deberiaExponerLosDatosConQueSeConstruyo() {
        // Arrange + Act
        Coupon cupon = new Coupon("PROMO20", 20.0, VENCE);

        // Assert
        assertEquals("PROMO20", cupon.codigo());
        assertEquals(20.0, cupon.montoDescuento(), DELTA);
        assertEquals(VENCE, cupon.fechaExpiracion());
    }

    @ParameterizedTest(name = "codigo=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("deberia rechazar un codigo nulo o vacio")
    void deberiaRechazarCodigoNuloOVacio(String codigo) {
        assertThrows(IllegalArgumentException.class, () -> new Coupon(codigo, 10.0, null));
    }

    @Test
    @DisplayName("deberia rechazar un monto de descuento negativo")
    void deberiaRechazarMontoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new Coupon("MALO", -1.0, null));
    }

    @Test
    @DisplayName("deberia no vencer nunca cuando no tiene fecha de expiracion")
    void deberiaNoVencerNuncaSinFechaDeExpiracion() {
        // Arrange
        Coupon cupon = new Coupon("SIEMPRE", 10.0, null);

        // Act + Assert
        assertFalse(cupon.estaExpirado(LocalDate.of(2099, 1, 1)));
    }

    @ParameterizedTest(name = "referencia={0} -> expirado={1}")
    @CsvSource({
        "2026-06-29, false",   // antes del vencimiento
        "2026-06-30, false",   // el mismo dia del vencimiento todavia es valido
        "2026-07-01, true"     // al dia siguiente ya vencio
    })
    @DisplayName("deberia vencer solo despues de la fecha de expiracion")
    void deberiaVencerSoloDespuesDeLaFechaDeExpiracion(LocalDate referencia, boolean esperado) {
        // Arrange
        Coupon cupon = new Coupon("PROMO20", 20.0, VENCE);

        // Act
        boolean expirado = cupon.estaExpirado(referencia);

        // Assert
        assertEquals(esperado, expirado);
    }
}
