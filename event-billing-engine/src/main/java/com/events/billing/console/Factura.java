package com.events.billing.console;

import com.events.billing.model.Customer;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static com.events.billing.console.Formato.centrar;
import static com.events.billing.console.Formato.money;
import static com.events.billing.console.Formato.porcentaje;
import static com.events.billing.console.Formato.raya;

/**
 * Resultado de una venta con su desglose. Agrupa los datos que antes se pasaban
 * como 9 parametros sueltos a imprimirFactura.
 *
 * El descuento por cupon y el ahorro se derivan de los demas valores.
 */
record Factura(Customer cliente, String cupon, List<ItemCarrito> items, LocalDate fecha,
               double subtotal, double descuentoCategoria, double total) {

    /** Formato de una fila "concepto ......... monto". */
    private static final String FILA = " %-24s %12s%n";

    Factura {
        items = List.copyOf(items);
    }

    double descuentoCupon() {
        return subtotal - descuentoCategoria - total;
    }

    double ahorro() {
        return subtotal - total;
    }

    void imprimirEn(PrintStream out) {
        out.println();
        out.println(raya('='));
        out.println(centrar("FACTURA DE VENTA"));
        out.println(raya('='));
        out.printf(" Cliente   : %s%n", cliente.getNombre());
        out.printf(" Categoria : %s (%s de descuento)%n",
                cliente.getCategoria(), porcentaje(cliente.getCategoria().getDiscountRate()));
        out.printf(" Fecha     : %s%n", fecha);
        if (cupon != null) {
            out.printf(" Cupon     : %s%n", cupon);
        }
        out.println(raya('-'));
        out.println(" DETALLE");
        for (ItemCarrito it : items) {
            out.printf(Locale.US, "  %2d x %-20s %10s%n", it.cantidad(), it.espectaculo().nombre(), money(it.subtotal()));
        }
        out.println(raya('-'));
        out.printf(Locale.US, FILA, "Subtotal", money(subtotal));
        out.printf(Locale.US, FILA, "Descuento categoria",
                descuentoCategoria > 0 ? "-" + money(descuentoCategoria) : money(0));
        if (cupon != null) {
            out.printf(Locale.US, FILA, "Descuento cupon", "-" + money(descuentoCupon()));
        }
        out.println(raya('-'));
        out.printf(Locale.US, FILA, "TOTAL A PAGAR", money(total));
        out.printf(Locale.US, FILA, "Ahorro total", money(ahorro()));
        out.println(raya('='));
    }
}
