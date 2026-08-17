package com.events.billing.discount;

import com.events.billing.model.Customer;
import com.events.billing.model.CustomerCategory;
import com.events.billing.model.Purchase;

/**
 * Regla de descuento por CATEGORIA DE CLIENTE.
 *
 * Tasas (definidas en el enum {@link CustomerCategory}):
 *   - STUDENT -> 15%
 *   - SENIOR  -> 20%
 *   - REGULAR -> 0%
 *
 * DECISION DE DISENO: si la categoria del cliente es null, se trata como
 * REGULAR (0% de descuento) en lugar de lanzar excepcion. Se eligio esta
 * opcion para que el motor sea tolerante ante datos incompletos y nunca falle
 * por una categoria faltante. (Puede cambiarse a lanzar excepcion si el
 * negocio lo exige.)
 */
public class CustomerCategoryDiscountRule implements DiscountRule {

    @Override
    public double apply(double amount, Purchase purchase) {
        Customer cliente = purchase.getCliente();
        CustomerCategory categoria = cliente.getCategoria();

        double tasa = (categoria == null) ? 0.0 : categoria.getDiscountRate();

        double resultado = amount - (amount * tasa);
        return Math.max(0.0, resultado); // por robustez: nunca negativo
    }
}
