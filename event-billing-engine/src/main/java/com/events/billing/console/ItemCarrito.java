package com.events.billing.console;

/** Linea del carrito: un espectaculo y cuantas entradas se compran de el. */
record ItemCarrito(Espectaculo espectaculo, int cantidad) {

    double subtotal() {
        return espectaculo.precio() * cantidad;
    }
}
