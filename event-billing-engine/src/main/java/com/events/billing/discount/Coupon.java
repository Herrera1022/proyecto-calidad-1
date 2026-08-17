package com.events.billing.discount;

import java.time.LocalDate;

/**
 * Representa un cupon de descuento de MONTO FIJO.
 * La fecha de expiracion es opcional: si es null, el cupon no vence.
 * Clase inmutable con validacion en el constructor.
 */
public final class Coupon {

    private final String codigo;
    private final double montoDescuento;
    private final LocalDate fechaExpiracion; // null = sin vencimiento

    public Coupon(String codigo, double montoDescuento, LocalDate fechaExpiracion) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo del cupon no puede ser nulo o vacio");
        }
        if (montoDescuento < 0) {
            throw new IllegalArgumentException("El monto de descuento no puede ser negativo: " + montoDescuento);
        }
        this.codigo = codigo;
        this.montoDescuento = montoDescuento;
        this.fechaExpiracion = fechaExpiracion;
    }

    /**
     * @param fechaReferencia normalmente la fecha de la compra.
     * @return true si el cupon esta vencido respecto a esa fecha.
     */
    public boolean estaExpirado(LocalDate fechaReferencia) {
        return fechaExpiracion != null && fechaReferencia.isAfter(fechaExpiracion);
    }

    public String getCodigo() {
        return codigo;
    }

    public double getMontoDescuento() {
        return montoDescuento;
    }

    public LocalDate getFechaExpiracion() {
        return fechaExpiracion;
    }
}
