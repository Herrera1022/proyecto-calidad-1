package com.events.billing.console;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Scanner;

/**
 * Lectura ROBUSTA de la entrada del usuario: valida y re-solicita ante errores
 * de digitacion (numeros invalidos, opciones fuera de rango, etc.).
 *
 * Recibe la entrada y la salida por constructor para poder probarla con datos
 * simulados.
 */
final class EntradaConsola {

    private final Scanner scanner;
    private final PrintStream out;

    EntradaConsola(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
    }

    /** Lee una linea. Si el flujo de entrada se cierra (EOF), lanza FinDeEntradaException. */
    String leerLinea() {
        if (!scanner.hasNextLine()) {
            throw new FinDeEntradaException();
        }
        return scanner.nextLine().trim();
    }

    /** Lee un entero dentro de [min, max]; re-solicita ante cualquier error. */
    int leerEntero(String mensaje, int min, int max) {
        while (true) {
            out.print(mensaje);
            String s = leerLinea();
            try {
                int valor = Integer.parseInt(s);
                if (valor < min || valor > max) {
                    out.printf("  > Debe ser un numero entre %d y %d.%n", min, max);
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                out.println("  > Entrada invalida. Escribe solo numeros (ej. " + min + ").");
            }
        }
    }

    /** Lee 's' o 'n' de forma tolerante. */
    boolean leerSiNo(String mensaje) {
        while (true) {
            out.print(mensaje);
            switch (leerLinea().toLowerCase(Locale.ROOT)) {
                case "s", "si", "sí", "y", "yes" -> { return true; }
                case "n", "no" -> { return false; }
                default -> out.println("  > Responde 's' (si) o 'n' (no).");
            }
        }
    }

    void pausa() {
        out.print("Presiona Enter para volver al menu...");
        leerLinea();
    }
}
