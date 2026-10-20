package com.events.billing.discount;

import java.time.LocalDate;

/**
 * Representa un cupon de descuento de MONTO FIJO.
 * La fecha de expiracion es opcional: si es null, el cupon no vence.
 * Inmutable (record) con validacion en el constructor.
 *
 * @param codigo          codigo del cupon (no nulo ni vacio)
 * @param montoDescuento  monto fijo a descontar (no negativo)
 * @param fechaExpiracion ultimo dia de validez; null = sin vencimiento
 */
public record Coupon(String codigo, double montoDescuento, LocalDate fechaExpiracion) {

    public Coupon {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo del cupon no puede ser nulo o vacio");
        }
        if (montoDescuento < 0) {
            throw new IllegalArgumentException("El monto de descuento no puede ser negativo: " + montoDescuento);
        }
    }

    /**
     * @param fechaReferencia normalmente la fecha de la compra.
     * @return true si el cupon esta vencido respecto a esa fecha.
     */
    public boolean estaExpirado(LocalDate fechaReferencia) {
        return fechaExpiracion != null && fechaReferencia.isAfter(fechaExpiracion);
    }
}
