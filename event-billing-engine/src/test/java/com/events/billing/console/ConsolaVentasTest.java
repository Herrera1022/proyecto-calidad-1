package com.events.billing.console;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la consola interactiva de punta a punta: se simula lo que el
 * usuario escribe (una linea por respuesta) y se revisa lo que la consola
 * imprime. El reloj es fijo para que las fechas sean reproducibles.
 */
@DisplayName("ConsolaVentas")
class ConsolaVentasTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-08-18T15:00:00Z"), ZoneOffset.UTC);

    /** Ejecuta la consola con las respuestas indicadas y devuelve lo que imprimio. */
    private static String ejecutar(String... respuestas) {
        String entrada = String.join("\n", respuestas) + "\n";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new ConsolaVentas(new Scanner(entrada), out, RELOJ).iniciar();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static String fila(String concepto, String monto) {
        return String.format(" %-24s %12s", concepto, monto);
    }

    @Test
    @DisplayName("deberia mostrar bienvenida y despedida al salir directamente")
    void deberiaMostrarBienvenidaYDespedidaAlSalir() {
        // Act
        String salida = ejecutar("5");

        // Assert
        assertTrue(salida.contains("BIENVENIDO"));
        assertTrue(salida.contains("MOTOR DE FACTURACION - EVENTOS"));
        assertTrue(salida.contains("Gracias por usar el sistema. Hasta pronto!"));
    }

    @Test
    @DisplayName("deberia registrar una venta con varios eventos, categoria y cupon")
    void deberiaRegistrarVentaCompletaConCupon() {
        // Arrange -> estudiante, 2 x Concierto Rock + 1 x Cine Premier, cupon promo20
        String[] respuestas = {"1", "Ana Gomez", "2", "1", "2", "3", "1", "0", "promo20", "", "5"};

        // Act
        String salida = ejecutar(respuestas);

        // Assert -> 275 - 15% (41.25) - 20 = 213.75
        assertTrue(salida.contains("Cupon aplicado: PROMO20 (-$20.00)"));
        assertTrue(salida.contains(" Fecha     : 2026-08-18"));
        assertTrue(salida.contains(fila("Subtotal", "$275.00")));
        assertTrue(salida.contains(fila("Descuento categoria", "-$41.25")));
        assertTrue(salida.contains(fila("Descuento cupon", "-$20.00")));
        assertTrue(salida.contains(fila("TOTAL A PAGAR", "$213.75")));
        assertTrue(salida.contains(fila("Ahorro total", "$61.25")));
    }

    @Test
    @DisplayName("deberia usar 'Cliente' como nombre cuando se deja vacio")
    void deberiaUsarNombrePorDefectoCuandoSeDejaVacio() {
        // Act -> regular, 1 x Cine Premier, sin cupon
        String salida = ejecutar("1", "", "1", "3", "1", "0", "", "", "5");

        // Assert
        assertTrue(salida.contains(" Cliente   : Cliente"));
        assertTrue(salida.contains(fila("TOTAL A PAGAR", "$35.00")));
        assertFalse(salida.contains("Descuento cupon"));
    }

    @Test
    @DisplayName("deberia exigir al menos un evento en el carrito")
    void deberiaExigirAlMenosUnEventoEnElCarrito() {
        // Act -> intenta terminar el carrito vacio y luego agrega un evento
        String salida = ejecutar("1", "Ana", "1", "0", "2", "1", "0", "", "", "5");

        // Assert
        assertTrue(salida.contains("Debes agregar al menos un evento a la compra."));
        assertTrue(salida.contains("Agregado: 1 x Conferencia Tech = $80.00"));
    }

    @Test
    @DisplayName("deberia continuar sin cupon cuando el cupon no existe y el usuario no reintenta")
    void deberiaContinuarSinCuponCuandoNoExiste() {
        // Act
        String salida = ejecutar("1", "Ana", "1", "3", "1", "0", "noexiste", "n", "", "5");

        // Assert
        assertTrue(salida.contains("El cupon 'noexiste' no existe."));
        assertFalse(salida.contains("Cupon     :"));
        assertTrue(salida.contains(fila("TOTAL A PAGAR", "$35.00")));
    }

    @Test
    @DisplayName("deberia rechazar un cupon vencido y permitir reintentar con otro")
    void deberiaRechazarCuponVencidoYPermitirReintentar() {
        // Act -> regular, 1 x Concierto Rock, cupon VIEJO (vencido) y luego DESC50
        String salida = ejecutar("1", "Luis", "1", "1", "1", "0", "viejo", "s", "desc50", "", "5");

        // Assert -> 120 - 50 = 70
        assertTrue(salida.contains("El cupon 'VIEJO' esta vencido."));
        assertTrue(salida.contains("Cupon aplicado: DESC50 (-$50.00)"));
        assertTrue(salida.contains(fila("TOTAL A PAGAR", "$70.00")));
    }

    @Test
    @DisplayName("deberia mostrar el historial vacio y luego con las ventas de la sesion")
    void deberiaMostrarHistorialDeLaSesion() {
        // Act -> historial vacio, venta senior de 1 x Conferencia Tech (80 - 20% = 64), historial
        String salida = ejecutar("4", "", "1", "Ana", "3", "2", "1", "0", "", "", "4", "", "5");

        // Assert
        assertTrue(salida.contains("Aun no hay ventas registradas en esta sesion."));
        assertTrue(salida.contains(" 1. Ana            SENIOR   Total:     $64.00  Ahorro:     $16.00"));
        assertTrue(salida.contains("Ventas: 1    Facturado: $64.00    Ahorrado: $16.00"));
    }

    @Test
    @DisplayName("deberia listar los cupones con su estado segun la fecha")
    void deberiaListarCuponesConSuEstado() {
        // Act
        String salida = ejecutar("2", "", "5");

        // Assert
        assertTrue(salida.contains("CUPONES DISPONIBLES"));
        assertTrue(salida.contains(" PROMO20          -$20.00      vigente"));
        assertTrue(salida.contains(" VIEJO            -$30.00      vencido"));
    }

    @Test
    @DisplayName("deberia listar las tarifas de cada categoria")
    void deberiaListarTarifasPorCategoria() {
        // Act
        String salida = ejecutar("3", "", "5");

        // Assert
        assertTrue(salida.contains(" REGULAR    -> 0% de descuento"));
        assertTrue(salida.contains(" STUDENT    -> 15% de descuento"));
        assertTrue(salida.contains(" SENIOR     -> 20% de descuento"));
    }

    @Test
    @DisplayName("deberia volver a preguntar ante una opcion invalida del menu")
    void deberiaVolverAPreguntarAnteOpcionInvalida() {
        // Act
        String salida = ejecutar("x", "9", "5");

        // Assert
        assertTrue(salida.contains("Entrada invalida. Escribe solo numeros (ej. 1)."));
        assertTrue(salida.contains("Debe ser un numero entre 1 y 5."));
        assertTrue(salida.contains("Gracias por usar el sistema. Hasta pronto!"));
    }

    @Test
    @DisplayName("deberia terminar limpio si la entrada se cierra a mitad de una venta")
    void deberiaTerminarLimpioSiLaEntradaSeCierra() {
        // Act -> la entrada termina despues del nombre del cliente
        String salida = ejecutar("1", "Pedro");

        // Assert
        assertTrue(salida.contains("Entrada finalizada. Hasta luego."));
        assertFalse(salida.contains("Gracias por usar el sistema"));
    }
}
