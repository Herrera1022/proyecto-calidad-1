package com.events.billing.console;

import java.util.Locale;

/**
 * Utilidades de formato de texto para la consola (montos, porcentajes,
 * separadores y centrado). No tiene estado.
 */
final class Formato {

    /** Ancho de las lineas de la consola. */
    static final int ANCHO = 54;

    private Formato() {
        // clase de utilidades: no se instancia
    }

    static String money(double v) {
        return String.format(Locale.US, "$%.2f", v);
    }

    static String porcentaje(double tasa) {
        return String.format(Locale.US, "%.0f%%", tasa * 100);
    }

    static String raya(char c) {
        return String.valueOf(c).repeat(ANCHO);
    }

    static String centrar(String texto) {
        if (texto.length() >= ANCHO) {
            return texto;
        }
        return " ".repeat((ANCHO - texto.length()) / 2) + texto;
    }

    static String recortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max - 1) + ".";
    }
}
