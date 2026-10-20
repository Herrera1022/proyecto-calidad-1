package com.events.billing.console;

import com.events.billing.DiscountEngine;
import com.events.billing.discount.Coupon;
import com.events.billing.discount.CouponDiscountRule;
import com.events.billing.discount.CustomerCategoryDiscountRule;
import com.events.billing.discount.DiscountRule;
import com.events.billing.model.Customer;
import com.events.billing.model.CustomerCategory;
import com.events.billing.model.Purchase;
import com.events.billing.model.Ticket;

import java.io.PrintStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import static com.events.billing.console.Formato.centrar;
import static com.events.billing.console.Formato.money;
import static com.events.billing.console.Formato.porcentaje;
import static com.events.billing.console.Formato.raya;
import static com.events.billing.console.Formato.recortar;

/**
 * Aplicacion de consola INTERACTIVA para el motor de facturacion.
 *
 * Ofrece un menu con: registrar una venta (con carrito de varios eventos y
 * cupones), ver cupones, ver tarifas por categoria e historial de la sesion.
 *
 * La entrada, la salida y el reloj se reciben por constructor: en produccion
 * son System.in, System.out y el reloj del sistema; en las pruebas se usan
 * datos simulados y una fecha fija.
 *
 * NOTA: esta clase solo consume la API publica del dominio; no modifica ninguna
 * regla ni entidad.
 */
public class ConsolaVentas {

    private final EntradaConsola entrada;
    private final PrintStream out;
    private final Clock reloj;

    private final List<Espectaculo> cartelera;
    private final Map<String, Coupon> cupones;
    private final DiscountRule reglaCategoria = new CustomerCategoryDiscountRule();
    private final DiscountRule reglaCupon;

    /** Ventas de la sesion (en memoria). */
    private final List<Factura> historial = new ArrayList<>();

    public ConsolaVentas(Scanner scanner, PrintStream out, Clock reloj) {
        this.entrada = new EntradaConsola(scanner, out);
        this.out = out;
        this.reloj = reloj;
        this.cartelera = Catalogo.cartelera(hoy());
        this.cupones = Catalogo.cupones();
        this.reglaCupon = new CouponDiscountRule(cupones);
    }

    /** Muestra el menu hasta que el usuario sale o se cierra la entrada. */
    public void iniciar() {
        imprimirBienvenida();
        try {
            ejecutarMenu();
            imprimirDespedida();
        } catch (FinDeEntradaException e) {
            out.println();
            out.println("Entrada finalizada. Hasta luego.");
        }
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    // ============================ MENU ============================

    private void ejecutarMenu() {
        boolean salir = false;
        while (!salir) {
            switch (menuPrincipal()) {
                case 1 -> registrarVenta();
                case 2 -> mostrarCupones();
                case 3 -> mostrarTarifas();
                case 4 -> mostrarHistorial();
                case 5 -> salir = true;
                default -> {
                    // menuPrincipal solo devuelve opciones entre 1 y 5
                }
            }
        }
    }

    private int menuPrincipal() {
        out.println();
        out.println(raya('='));
        out.println(centrar("MOTOR DE FACTURACION - EVENTOS"));
        out.println(raya('='));
        out.println("  1) Registrar una venta");
        out.println("  2) Ver cupones disponibles");
        out.println("  3) Ver tarifas por categoria");
        out.println("  4) Historial de la sesion");
        out.println("  5) Salir");
        out.println(raya('-'));
        return entrada.leerEntero("Selecciona una opcion (1-5): ", 1, 5);
    }

    // ======================= REGISTRAR VENTA =======================

    private void registrarVenta() {
        out.println();
        out.println(centrar("--- NUEVA VENTA ---"));

        // 1) Cliente
        out.print("Nombre del cliente (Enter = \"Cliente\"): ");
        String nombre = entrada.leerLinea();
        if (nombre.isBlank()) {
            nombre = "Cliente";
        }

        // 2) Categoria
        CustomerCategory categoria = leerCategoria();
        Customer cliente = new Customer("C1", nombre, categoria);

        // 3) Carrito (uno o varios eventos)
        List<ItemCarrito> items = leerCarrito();

        // 4) Cupon (opcional, con reintento)
        String cuponCode = leerCupon();

        // 5) Se construye la compra
        Purchase venta = new Purchase(cliente, construirTickets(items), cuponCode, hoy());

        // 6) Calculo con desglose (usando las reglas del dominio)
        double subtotal = venta.getSubtotal();
        double trasCategoria = reglaCategoria.apply(subtotal, venta);
        double total = (cuponCode != null)
                ? new DiscountEngine(List.of(reglaCategoria, reglaCupon)).calculateTotal(venta)
                : trasCategoria;

        // 7) Factura e historial
        Factura factura = new Factura(cliente, cuponCode, items, hoy(), subtotal, subtotal - trasCategoria, total);
        factura.imprimirEn(out);
        historial.add(factura);

        entrada.pausa();
    }

    /** Genera un ticket por cada entrada del carrito, numerados T1, T2, ... */
    private static List<Ticket> construirTickets(List<ItemCarrito> items) {
        List<Ticket> tickets = new ArrayList<>();
        for (ItemCarrito it : items) {
            for (int k = 0; k < it.cantidad(); k++) {
                tickets.add(new Ticket("T" + (tickets.size() + 1), it.espectaculo().evento(), it.espectaculo().precio()));
            }
        }
        return tickets;
    }

    private CustomerCategory leerCategoria() {
        out.println("Categoria del cliente:");
        out.println("  1) REGULAR  (0% de descuento)");
        out.println("  2) STUDENT  (15% de descuento)");
        out.println("  3) SENIOR   (20% de descuento)");
        int op = entrada.leerEntero("Selecciona (1-3): ", 1, 3);
        return switch (op) {
            case 2 -> CustomerCategory.STUDENT;
            case 3 -> CustomerCategory.SENIOR;
            default -> CustomerCategory.REGULAR;
        };
    }

    private List<ItemCarrito> leerCarrito() {
        List<ItemCarrito> items = new ArrayList<>();
        while (true) {
            mostrarCartelera();
            int op = entrada.leerEntero("Agregar evento (0-" + cartelera.size() + "): ", 0, cartelera.size());
            if (op == 0 && !items.isEmpty()) {
                return items;
            }
            if (op == 0) {
                out.println("  > Debes agregar al menos un evento a la compra.");
                continue;
            }
            Espectaculo elegido = cartelera.get(op - 1);
            int cantidad = entrada.leerEntero("  Cuantas entradas? (1-50): ", 1, 50);
            ItemCarrito item = new ItemCarrito(elegido, cantidad);
            items.add(item);

            double subtotalActual = items.stream().mapToDouble(ItemCarrito::subtotal).sum();
            out.printf(Locale.US, "  > Agregado: %d x %s = %s   |   Subtotal actual: %s%n",
                    cantidad, elegido.nombre(), money(item.subtotal()), money(subtotalActual));
        }
    }

    private void mostrarCartelera() {
        out.println();
        out.println("Cartelera:");
        for (int i = 0; i < cartelera.size(); i++) {
            Espectaculo e = cartelera.get(i);
            out.printf(Locale.US, "  %d) %-18s %8s%n", i + 1, e.nombre(), money(e.precio()));
        }
        out.println("  0) Terminar y continuar");
    }

    private String leerCupon() {
        while (true) {
            out.print("Codigo de cupon (Enter = ninguno): ");
            String code = entrada.leerLinea();
            if (code.isBlank()) {
                return null;
            }
            String upper = code.toUpperCase(Locale.ROOT);
            if (cuponAplicable(code, upper)) {
                return upper;
            }
            if (!entrada.leerSiNo("  > Intentar con otro cupon? (s/n): ")) {
                return null;
            }
        }
    }

    /** Informa al usuario si el cupon existe y esta vigente; devuelve true si se puede aplicar. */
    private boolean cuponAplicable(String code, String upper) {
        Coupon cupon = cupones.get(upper);
        if (cupon == null) {
            out.println("  > El cupon '" + code + "' no existe.");
            return false;
        }
        if (cupon.estaExpirado(hoy())) {
            out.println("  > El cupon '" + upper + "' esta vencido.");
            return false;
        }
        out.printf(Locale.US, "  > Cupon aplicado: %s (-%s)%n", upper, money(cupon.getMontoDescuento()));
        return true;
    }

    // ========================= OTRAS VISTAS =========================

    private void mostrarCupones() {
        out.println();
        out.println(raya('='));
        out.println(centrar("CUPONES DISPONIBLES"));
        out.println(raya('='));
        out.printf(" %-16s %-12s %-12s%n", "CODIGO", "DESCUENTO", "ESTADO");
        out.println(raya('-'));
        for (Coupon c : cupones.values()) {
            String estado = c.estaExpirado(hoy()) ? "vencido" : "vigente";
            out.printf(Locale.US, " %-16s %-12s %-12s%n", c.getCodigo(), "-" + money(c.getMontoDescuento()), estado);
        }
        out.println(raya('='));
        entrada.pausa();
    }

    private void mostrarTarifas() {
        out.println();
        out.println(raya('='));
        out.println(centrar("TARIFAS POR CATEGORIA"));
        out.println(raya('='));
        for (CustomerCategory c : CustomerCategory.values()) {
            out.printf(" %-10s -> %s de descuento%n", c, porcentaje(c.getDiscountRate()));
        }
        out.println(raya('='));
        entrada.pausa();
    }

    private void mostrarHistorial() {
        out.println();
        out.println(raya('='));
        out.println(centrar("HISTORIAL DE LA SESION"));
        out.println(raya('='));
        if (historial.isEmpty()) {
            out.println(" Aun no hay ventas registradas en esta sesion.");
            out.println(raya('='));
            entrada.pausa();
            return;
        }
        double totFacturado = 0;
        double totAhorrado = 0;
        int i = 1;
        for (Factura f : historial) {
            out.printf(Locale.US, " %d. %-14s %-8s Total: %10s  Ahorro: %10s%n",
                    i++, recortar(f.cliente().getNombre(), 14), f.cliente().getCategoria(),
                    money(f.total()), money(f.ahorro()));
            totFacturado += f.total();
            totAhorrado += f.ahorro();
        }
        out.println(raya('-'));
        out.printf(Locale.US, " Ventas: %d    Facturado: %s    Ahorrado: %s%n",
                historial.size(), money(totFacturado), money(totAhorrado));
        out.println(raya('='));
        entrada.pausa();
    }

    // ========================= BIENVENIDA =========================

    private void imprimirBienvenida() {
        out.println(raya('='));
        out.println(centrar("BIENVENIDO"));
        out.println(centrar("Sistema de venta de entradas"));
        out.println(raya('='));
    }

    private void imprimirDespedida() {
        out.println();
        out.println(raya('='));
        out.println(centrar("Gracias por usar el sistema. Hasta pronto!"));
        out.println(raya('='));
    }
}
