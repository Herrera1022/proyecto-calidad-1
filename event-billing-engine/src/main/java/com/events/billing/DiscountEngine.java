package com.events.billing;

import com.events.billing.discount.DiscountRule;
import com.events.billing.model.Purchase;

import java.util.List;
import java.util.Objects;

/**
 * Motor de facturacion. Calcula el total de una compra aplicando una lista de
 * reglas de descuento.
 *
 * ORDEN DE APLICACION (importante): las reglas se aplican de forma SECUENCIAL,
 * en el mismo orden en que aparecen en la lista recibida por el constructor.
 * El resultado de una regla es la entrada de la siguiente. Por lo tanto, el
 * orden lo controla quien construye el motor.
 *
 * El motor soporta:
 *   - una sola regla,
 *   - varias reglas combinadas,
 *   - ninguna regla (lista vacia) -> el total es igual al subtotal.
 *
 * Garantia: el total final nunca es negativo (minimo 0).
 */
public class DiscountEngine {

    private final List<DiscountRule> reglas;

    public DiscountEngine(List<DiscountRule> reglas) {
        Objects.requireNonNull(reglas, "la lista de reglas no puede ser nula (puede estar vacia)");
        this.reglas = List.copyOf(reglas); // copia inmutable; admite lista vacia
    }

    /**
     * Calcula el total de la compra:
     *   1. parte del subtotal (suma de precios de los tickets),
     *   2. aplica cada regla en orden (encadenadas),
     *   3. garantiza que el resultado nunca sea negativo.
     */
    public double calculateTotal(Purchase purchase) {
        Objects.requireNonNull(purchase, "la compra no puede ser nula");

        double total = purchase.getSubtotal();

        for (DiscountRule regla : reglas) {
            total = regla.apply(total, purchase);
        }

        return Math.max(0.0, total);
    }
}
