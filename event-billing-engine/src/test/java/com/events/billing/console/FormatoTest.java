package com.events.billing.console;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Formato")
class FormatoTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "0.0,     $0.00",
        "35.0,    $35.00",
        "213.75,  $213.75",
        "1234.567, $1234.57"   // redondea a 2 decimales
    })
    @DisplayName("deberia formatear montos con signo $ y 2 decimales")
    void deberiaFormatearMontos(double valor, String esperado) {
        assertEquals(esperado, Formato.money(valor));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "0.0,  0%",
        "0.15, 15%",
        "0.20, 20%"
    })
    @DisplayName("deberia formatear tasas como porcentaje entero")
    void deberiaFormatearPorcentajes(double tasa, String esperado) {
        assertEquals(esperado, Formato.porcentaje(tasa));
    }

    @Test
    @DisplayName("deberia generar una raya del ancho de la consola")
    void deberiaGenerarRayaDelAnchoDeLaConsola() {
        assertEquals("=".repeat(Formato.ANCHO), Formato.raya('='));
    }

    @Test
    @DisplayName("deberia centrar un texto corto agregando espacios a la izquierda")
    void deberiaCentrarTextoCorto() {
        // Arrange
        String texto = "HOLA";

        // Act
        String centrado = Formato.centrar(texto);

        // Assert -> (54 - 4) / 2 = 25 espacios
        assertEquals(" ".repeat(25) + "HOLA", centrado);
    }

    @Test
    @DisplayName("deberia dejar intacto un texto igual o mas largo que el ancho")
    void deberiaDejarIntactoTextoLargo() {
        // Arrange
        String largo = "x".repeat(Formato.ANCHO);

        // Act + Assert
        assertSame(largo, Formato.centrar(largo));
    }

    @ParameterizedTest(name = "[{0}] max={1} -> [{2}]")
    @CsvSource({
        "Ana,                      14, Ana",
        "Maria Fernanda,           14, Maria Fernanda",
        "Maria Fernanda Rodriguez, 14, Maria Fernand."
    })
    @DisplayName("deberia recortar solo los textos que superan el maximo")
    void deberiaRecortarTextosLargos(String texto, int max, String esperado) {
        assertEquals(esperado, Formato.recortar(texto, max));
    }
}
