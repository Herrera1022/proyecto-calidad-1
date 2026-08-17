package com.events.billing.model;

/**
 * Categoria del cliente. Cada categoria lleva asociada su tasa de descuento
 * fija, expresada como fraccion (0.15 = 15%).
 */
public enum CustomerCategory {

    REGULAR(0.0),   // sin descuento
    STUDENT(0.15),  // 15%
    SENIOR(0.20);   // 20% (tercera edad)

    private final double discountRate;

    CustomerCategory(double discountRate) {
        this.discountRate = discountRate;
    }

    /** @return la tasa de descuento como fraccion (0.15 = 15%). */
    public double getDiscountRate() {
        return discountRate;
    }
}
