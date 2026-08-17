package com.events.billing.discount;

import com.events.billing.exception.InvalidCouponException;
import com.events.billing.model.Purchase;

import java.util.Map;
import java.util.Objects;

/**
 * Regla de descuento por CUPON de monto fijo.
 *
 * DECISION DE DISENO: la regla se construye con un repositorio de cupones
 * validos (un Map codigo -> Coupon). El codigo a aplicar se toma de la compra
 * ({@code purchase.getCodigoCupon()}), de modo que la misma regla sirve para
 * cualquier compra sin recrearla.
 *
 * Validaciones (todas lanzan {@link InvalidCouponException}):
 *   - codigo nulo o vacio,
 *   - cupon inexistente en el repositorio,
 *   - cupon vencido (respecto a la fecha de la compra).
 *
 * Si el monto del cupon supera el total, el resultado se limita a 0 (nunca negativo).
 */
public class CouponDiscountRule implements DiscountRule {

    private final Map<String, Coupon> cuponesValidos;

    public CouponDiscountRule(Map<String, Coupon> cuponesValidos) {
        Objects.requireNonNull(cuponesValidos, "el repositorio de cupones no puede ser nulo");
        this.cuponesValidos = Map.copyOf(cuponesValidos); // copia inmutable
    }

    @Override
    public double apply(double amount, Purchase purchase) {
        String codigo = purchase.getCodigoCupon();

        if (codigo == null || codigo.isBlank()) {
            throw new InvalidCouponException("El codigo de cupon es nulo o vacio");
        }

        Coupon cupon = cuponesValidos.get(codigo);
        if (cupon == null) {
            throw new InvalidCouponException("El cupon no existe: " + codigo);
        }

        if (cupon.estaExpirado(purchase.getFechaCompra())) {
            throw new InvalidCouponException("El cupon esta vencido: " + codigo);
        }

        double resultado = amount - cupon.getMontoDescuento();
        return Math.max(0.0, resultado); // el total nunca queda negativo
    }
}
