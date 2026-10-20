package com.events.billing.console;

/**
 * Se lanza cuando el flujo de entrada se cierra (EOF). La consola la captura
 * para terminar de forma limpia, sin llamar a System.exit.
 */
class FinDeEntradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    FinDeEntradaException() {
        super("El flujo de entrada se cerro");
    }
}
