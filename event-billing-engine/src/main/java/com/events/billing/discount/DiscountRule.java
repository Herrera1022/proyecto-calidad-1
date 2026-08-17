package com.events.billing.discount;

import com.events.billing.model.Purchase;

/**
 * Contrato comun a todas las reglas de descuento.
 *
 * Cada regla recibe el monto actual y la compra, y devuelve el nuevo monto
 * tras aplicar su descuento. Esto permite ENCADENAR reglas: la salida de una
 * es la entrada de la siguiente (ver {@code DiscountEngine}).
 */
public interface DiscountRule {

    /**
     * @param amount   monto sobre el cual se aplica el descuento.
     * @param purchase la compra (aporta cliente, cupon, fecha, etc.).
     * @return el nuevo monto tras aplicar la regla (nunca negativo).
     */
    double apply(double amount, Purchase purchase);
}
