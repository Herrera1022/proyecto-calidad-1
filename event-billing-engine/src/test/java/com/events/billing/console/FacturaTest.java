package com.events.billing.console;

import com.events.billing.model.Customer;
import com.events.billing.model.CustomerCategory;
import com.events.billing.model.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Factura")
class FacturaTest {

    private static final double DELTA = 1e-9;
    private static final LocalDate FECHA = LocalDate.of(2026, 8, 18);

    private final Espectaculo rock =
            new Espectaculo(new Event("EV1", "Concierto Rock", FECHA.plusMonths(2), "concierto"), 120.0);
    private final Customer estudiante = new Customer("C1", "Ana Gomez", CustomerCategory.STUDENT);

    /** Fila "concepto ...... monto" con el mismo formato que usa la factura. */
    private static String fila(String concepto, String monto) {
        return String.format(" %-24s %12s", concepto, monto);
    }

    private static String imprimir(Factura factura) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        factura.imprimirEn(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("deberia derivar el descuento por cupon y el ahorro total")
    void deberiaDerivarDescuentoCuponYAhorro() {
        // Arrange -> 2 x 120 = 240; 15% = 36; cupon de 20 -> total 184
        Factura factura = new Factura(estudiante, "PROMO20", List.of(new ItemCarrito(rock, 2)),
                FECHA, 240.0, 36.0, 184.0);

        // Act + Assert
        assertEquals(20.0, factura.descuentoCupon(), DELTA);
        assertEquals(56.0, factura.ahorro(), DELTA);
    }

    @Test
    @DisplayName("deberia imprimir el desglose completo cuando hay cupon")
    void deberiaImprimirDesgloseConCupon() {
        // Arrange
        Factura factura = new Factura(estudiante, "PROMO20", List.of(new ItemCarrito(rock, 2)),
                FECHA, 240.0, 36.0, 184.0);

        // Act
        String texto = imprimir(factura);

        // Assert
        assertTrue(texto.contains(" Cliente   : Ana Gomez"));
        assertTrue(texto.contains(" Categoria : STUDENT (15% de descuento)"));
        assertTrue(texto.contains(" Fecha     : 2026-08-18"));
        assertTrue(texto.contains(" Cupon     : PROMO20"));
        assertTrue(texto.contains("   2 x Concierto Rock          $240.00"));
        assertTrue(texto.contains(fila("Descuento categoria", "-$36.00")));
        assertTrue(texto.contains(fila("Descuento cupon", "-$20.00")));
        assertTrue(texto.contains(fila("TOTAL A PAGAR", "$184.00")));
        assertTrue(texto.contains(fila("Ahorro total", "$56.00")));
    }

    @Test
    @DisplayName("deberia omitir las lineas de cupon cuando no hay cupon")
    void deberiaOmitirLineasDeCuponSinCupon() {
        // Arrange -> cliente regular, sin descuentos
        Customer regular = new Customer("C1", "Luis", CustomerCategory.REGULAR);
        Factura factura = new Factura(regular, null, List.of(new ItemCarrito(rock, 1)),
                FECHA, 120.0, 0.0, 120.0);

        // Act
        String texto = imprimir(factura);

        // Assert
        assertEquals(0.0, factura.descuentoCupon(), DELTA);
        assertFalse(texto.contains("Cupon     :"));
        assertFalse(texto.contains("Descuento cupon"));
        assertTrue(texto.contains(fila("Descuento categoria", "$0.00")));
        assertTrue(texto.contains(fila("TOTAL A PAGAR", "$120.00")));
    }

    @Test
    @DisplayName("deberia copiar la lista de items para no depender del carrito original")
    void deberiaCopiarLaListaDeItems() {
        // Arrange
        List<ItemCarrito> carrito = new ArrayList<>(List.of(new ItemCarrito(rock, 1)));
        Factura factura = new Factura(estudiante, null, carrito, FECHA, 120.0, 18.0, 102.0);

        // Act
        carrito.add(new ItemCarrito(rock, 5));

        // Assert
        assertEquals(1, factura.items().size());
    }
}
