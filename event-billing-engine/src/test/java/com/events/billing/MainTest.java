package com.events.billing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifica que el punto de entrada conecte la consola con la entrada/salida estandar. */
@DisplayName("Main")
class MainTest {

    private InputStream entradaOriginal;
    private PrintStream salidaOriginal;

    @BeforeEach
    void guardarEntradaYSalidaEstandar() {
        entradaOriginal = System.in;
        salidaOriginal = System.out;
    }

    @AfterEach
    void restaurarEntradaYSalidaEstandar() {
        System.setIn(entradaOriginal);
        System.setOut(salidaOriginal);
    }

    @Test
    @DisplayName("deberia iniciar la consola con System.in y System.out")
    void deberiaIniciarLaConsolaConLaEntradaYSalidaEstandar() {
        // Arrange -> el usuario elige "5) Salir"
        System.setIn(new ByteArrayInputStream("5\n".getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));

        // Act
        Main.main(new String[0]);

        // Assert
        String salida = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(salida.contains("BIENVENIDO"));
        assertTrue(salida.contains("Gracias por usar el sistema. Hasta pronto!"));
    }
}
