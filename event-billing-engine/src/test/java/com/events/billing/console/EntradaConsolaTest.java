package com.events.billing.console;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EntradaConsola")
class EntradaConsolaTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /** Crea una entrada que "escribe" las lineas indicadas, como si fuera el usuario. */
    private EntradaConsola entradaCon(String texto) {
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        return new EntradaConsola(new Scanner(texto), out);
    }

    private String salida() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("deberia leer una linea sin espacios al inicio ni al final")
    void deberiaLeerLineaSinEspacios() {
        // Arrange
        EntradaConsola entrada = entradaCon("   Ana Gomez   \n");

        // Act
        String linea = entrada.leerLinea();

        // Assert
        assertEquals("Ana Gomez", linea);
    }

    @Test
    @DisplayName("deberia lanzar FinDeEntradaException cuando se cierra la entrada")
    void deberiaLanzarExcepcionCuandoSeCierraLaEntrada() {
        // Arrange
        EntradaConsola entrada = entradaCon("");

        // Act + Assert
        assertThrows(FinDeEntradaException.class, entrada::leerLinea);
    }

    @Test
    @DisplayName("deberia devolver el entero cuando esta dentro del rango")
    void deberiaDevolverEnteroDentroDelRango() {
        // Arrange
        EntradaConsola entrada = entradaCon("3\n");

        // Act
        int valor = entrada.leerEntero("Opcion: ", 1, 5);

        // Assert
        assertEquals(3, valor);
        assertTrue(salida().contains("Opcion: "));
    }

    @Test
    @DisplayName("deberia volver a preguntar ante texto no numerico o fuera de rango")
    void deberiaVolverAPreguntarAnteEntradaInvalida() {
        // Arrange -> primero letras, luego un numero fuera de rango, luego uno valido
        EntradaConsola entrada = entradaCon("abc\n9\n2\n");

        // Act
        int valor = entrada.leerEntero("Opcion: ", 1, 5);

        // Assert
        assertEquals(2, valor);
        assertTrue(salida().contains("Entrada invalida. Escribe solo numeros (ej. 1)."));
        assertTrue(salida().contains("Debe ser un numero entre 1 y 5."));
    }

    @ParameterizedTest(name = "[{0}] -> si")
    @ValueSource(strings = {"s", "si", "sí", "y", "yes", "SI", "  S  "})
    @DisplayName("deberia aceptar variantes de 'si'")
    void deberiaAceptarVariantesDeSi(String respuesta) {
        assertTrue(entradaCon(respuesta + "\n").leerSiNo("Seguir? "));
    }

    @ParameterizedTest(name = "[{0}] -> no")
    @ValueSource(strings = {"n", "no", "NO"})
    @DisplayName("deberia aceptar variantes de 'no'")
    void deberiaAceptarVariantesDeNo(String respuesta) {
        assertFalse(entradaCon(respuesta + "\n").leerSiNo("Seguir? "));
    }

    @Test
    @DisplayName("deberia volver a preguntar si la respuesta no es si ni no")
    void deberiaVolverAPreguntarSiLaRespuestaNoEsSiNiNo() {
        // Arrange
        EntradaConsola entrada = entradaCon("quizas\nn\n");

        // Act
        boolean respuesta = entrada.leerSiNo("Seguir? ");

        // Assert
        assertFalse(respuesta);
        assertTrue(salida().contains("Responde 's' (si) o 'n' (no)."));
    }

    @Test
    @DisplayName("deberia esperar un Enter en la pausa")
    void deberiaEsperarEnterEnLaPausa() {
        // Arrange
        EntradaConsola entrada = entradaCon("\n");

        // Act
        entrada.pausa();

        // Assert
        assertTrue(salida().contains("Presiona Enter para volver al menu..."));
    }
}
