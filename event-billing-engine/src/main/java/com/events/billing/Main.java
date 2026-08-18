package com.events.billing;

import com.events.billing.discount.Coupon;
import com.events.billing.discount.CouponDiscountRule;
import com.events.billing.discount.CustomerCategoryDiscountRule;
import com.events.billing.discount.DiscountRule;
import com.events.billing.model.Customer;
import com.events.billing.model.CustomerCategory;
import com.events.billing.model.Event;
import com.events.billing.model.Purchase;
import com.events.billing.model.Ticket;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * Aplicacion de consola INTERACTIVA para el motor de facturacion.
 *
 * Ofrece un menu con: registrar una venta (con carrito de varios eventos y
 * cupones), ver cupones, ver tarifas por categoria e historial de la sesion.
 * Toda la entrada del usuario se valida y se re-solicita ante errores de
 * digitacion (numeros invalidos, opciones fuera de rango, etc.).
 *
 * Se ejecuta con:  gradlew run -q --console=plain
 *
 * NOTA: esta clase solo consume la API publica del dominio; no modifica ninguna
 * regla, entidad ni test del proyecto.
 */
public class Main {

    private static final int ANCHO = 54;
    private static final Scanner scanner = new Scanner(System.in);

    // --- Catalogo de eventos (cartelera) ---
    private static final List<Espectaculo> CARTELERA = List.of(
            new Espectaculo(new Event("EV1", "Concierto Rock", LocalDate.now().plusMonths(2), "concierto"), 120.0),
            new Espectaculo(new Event("EV2", "Conferencia Tech", LocalDate.now().plusMonths(1), "conferencia"), 80.0),
            new Espectaculo(new Event("EV3", "Cine Premier", LocalDate.now().plusWeeks(1), "cine"), 35.0)
    );

    // --- Catalogo de cupones ---
    private static final Map<String, Coupon> CUPONES = crearCupones();

    // --- Reglas de descuento (se crean una sola vez) ---
    private static final DiscountRule REGLA_CATEGORIA = new CustomerCategoryDiscountRule();
    private static final DiscountRule REGLA_CUPON = new CouponDiscountRule(CUPONES);

    // --- Historial de ventas de la sesion (en memoria) ---
    private static final List<Registro> HISTORIAL = new ArrayList<>();

    // Tipos auxiliares solo para la UI (no son entidades del dominio)
    private record Espectaculo(Event evento, double precio) {}
    private record ItemCarrito(Espectaculo esp, int cantidad) {}
    private record Registro(String cliente, CustomerCategory categoria, String cupon,
                            double subtotal, double total, double ahorro) {}

    public static void main(String[] args) {
        imprimirBienvenida();
        boolean salir = false;
        while (!salir) {
            switch (menuPrincipal()) {
                case 1 -> registrarVenta();
                case 2 -> mostrarCupones();
                case 3 -> mostrarTarifas();
                case 4 -> mostrarHistorial();
                case 5 -> salir = true;
                default -> {}
            }
        }
        imprimirDespedida();
    }

    // ============================ MENU ============================

    private static int menuPrincipal() {
        System.out.println();
        System.out.println(raya('='));
        System.out.println(centrar("MOTOR DE FACTURACION - EVENTOS"));
        System.out.println(raya('='));
        System.out.println("  1) Registrar una venta");
        System.out.println("  2) Ver cupones disponibles");
        System.out.println("  3) Ver tarifas por categoria");
        System.out.println("  4) Historial de la sesion");
        System.out.println("  5) Salir");
        System.out.println(raya('-'));
        return leerEntero("Selecciona una opcion (1-5): ", 1, 5);
    }

    // ======================= REGISTRAR VENTA =======================

    private static void registrarVenta() {
        System.out.println();
        System.out.println(centrar("--- NUEVA VENTA ---"));

        // 1) Cliente
        System.out.print("Nombre del cliente (Enter = \"Cliente\"): ");
        String nombre = leerLinea();
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
        List<Ticket> tickets = new ArrayList<>();
        int idTicket = 1;
        for (ItemCarrito it : items) {
            for (int k = 0; k < it.cantidad(); k++) {
                tickets.add(new Ticket("T" + (idTicket++), it.esp().evento(), it.esp().precio()));
            }
        }
        Purchase venta = new Purchase(cliente, tickets, cuponCode, LocalDate.now());

        // 6) Calculo con desglose (usando las reglas del dominio)
        double subtotal = venta.getSubtotal();
        double trasCategoria = REGLA_CATEGORIA.apply(subtotal, venta);
        double descCategoria = subtotal - trasCategoria;

        double total;
        double descCupon;
        if (cuponCode != null) {
            total = new DiscountEngine(List.of(REGLA_CATEGORIA, REGLA_CUPON)).calculateTotal(venta);
            descCupon = trasCategoria - total;
        } else {
            total = trasCategoria;
            descCupon = 0.0;
        }
        double ahorro = subtotal - total;

        // 7) Factura
        imprimirFactura(nombre, categoria, cuponCode, items, subtotal, descCategoria, descCupon, total, ahorro);

        // 8) Historial
        HISTORIAL.add(new Registro(nombre, categoria, cuponCode, subtotal, total, ahorro));

        pausa();
    }

    private static CustomerCategory leerCategoria() {
        System.out.println("Categoria del cliente:");
        System.out.println("  1) REGULAR  (0% de descuento)");
        System.out.println("  2) STUDENT  (15% de descuento)");
        System.out.println("  3) SENIOR   (20% de descuento)");
        int op = leerEntero("Selecciona (1-3): ", 1, 3);
        return switch (op) {
            case 2 -> CustomerCategory.STUDENT;
            case 3 -> CustomerCategory.SENIOR;
            default -> CustomerCategory.REGULAR;
        };
    }

    private static List<ItemCarrito> leerCarrito() {
        List<ItemCarrito> items = new ArrayList<>();
        while (true) {
            System.out.println();
            System.out.println("Cartelera:");
            for (int i = 0; i < CARTELERA.size(); i++) {
                Espectaculo e = CARTELERA.get(i);
                System.out.printf(Locale.US, "  %d) %-18s %8s%n", i + 1, e.evento().getNombre(), money(e.precio()));
            }
            System.out.println("  0) Terminar y continuar");

            int op = leerEntero("Agregar evento (0-" + CARTELERA.size() + "): ", 0, CARTELERA.size());
            if (op == 0) {
                if (items.isEmpty()) {
                    System.out.println("  > Debes agregar al menos un evento a la compra.");
                    continue;
                }
                return items;
            }
            Espectaculo elegido = CARTELERA.get(op - 1);
            int cantidad = leerEntero("  Cuantas entradas? (1-50): ", 1, 50);
            items.add(new ItemCarrito(elegido, cantidad));

            double subtotalActual = items.stream().mapToDouble(x -> x.esp().precio() * x.cantidad()).sum();
            System.out.printf(Locale.US, "  > Agregado: %d x %s = %s   |   Subtotal actual: %s%n",
                    cantidad, elegido.evento().getNombre(), money(elegido.precio() * cantidad), money(subtotalActual));
        }
    }

    private static String leerCupon() {
        while (true) {
            System.out.print("Codigo de cupon (Enter = ninguno): ");
            String code = leerLinea();
            if (code.isBlank()) {
                return null;
            }
            String upper = code.toUpperCase(Locale.ROOT);
            Coupon cupon = CUPONES.get(upper);

            if (cupon == null) {
                System.out.println("  > El cupon '" + code + "' no existe.");
            } else if (cupon.estaExpirado(LocalDate.now())) {
                System.out.println("  > El cupon '" + upper + "' esta vencido.");
            } else {
                System.out.printf(Locale.US, "  > Cupon aplicado: %s (-%s)%n", upper, money(cupon.getMontoDescuento()));
                return upper;
            }
            if (!leerSiNo("  > Intentar con otro cupon? (s/n): ")) {
                return null;
            }
        }
    }

    private static void imprimirFactura(String nombre, CustomerCategory categoria, String cupon,
                                        List<ItemCarrito> items, double subtotal, double descCategoria,
                                        double descCupon, double total, double ahorro) {
        System.out.println();
        System.out.println(raya('='));
        System.out.println(centrar("FACTURA DE VENTA"));
        System.out.println(raya('='));
        System.out.printf(" Cliente   : %s%n", nombre);
        System.out.printf(" Categoria : %s (%s de descuento)%n", categoria, porcentaje(categoria.getDiscountRate()));
        System.out.printf(" Fecha     : %s%n", LocalDate.now());
        if (cupon != null) {
            System.out.printf(" Cupon     : %s%n", cupon);
        }
        System.out.println(raya('-'));
        System.out.println(" DETALLE");
        for (ItemCarrito it : items) {
            System.out.printf(Locale.US, "  %2d x %-20s %10s%n",
                    it.cantidad(), it.esp().evento().getNombre(), money(it.esp().precio() * it.cantidad()));
        }
        System.out.println(raya('-'));
        System.out.printf(Locale.US, " %-24s %12s%n", "Subtotal", money(subtotal));
        System.out.printf(Locale.US, " %-24s %12s%n", "Descuento categoria", descCategoria > 0 ? "-" + money(descCategoria) : money(0));
        if (cupon != null) {
            System.out.printf(Locale.US, " %-24s %12s%n", "Descuento cupon", "-" + money(descCupon));
        }
        System.out.println(raya('-'));
        System.out.printf(Locale.US, " %-24s %12s%n", "TOTAL A PAGAR", money(total));
        System.out.printf(Locale.US, " %-24s %12s%n", "Ahorro total", money(ahorro));
        System.out.println(raya('='));
    }

    // ========================= OTRAS VISTAS =========================

    private static void mostrarCupones() {
        System.out.println();
        System.out.println(raya('='));
        System.out.println(centrar("CUPONES DISPONIBLES"));
        System.out.println(raya('='));
        System.out.printf(" %-16s %-12s %-12s%n", "CODIGO", "DESCUENTO", "ESTADO");
        System.out.println(raya('-'));
        for (Coupon c : CUPONES.values()) {
            String estado = c.estaExpirado(LocalDate.now()) ? "vencido" : "vigente";
            System.out.printf(Locale.US, " %-16s %-12s %-12s%n", c.getCodigo(), "-" + money(c.getMontoDescuento()), estado);
        }
        System.out.println(raya('='));
        pausa();
    }

    private static void mostrarTarifas() {
        System.out.println();
        System.out.println(raya('='));
        System.out.println(centrar("TARIFAS POR CATEGORIA"));
        System.out.println(raya('='));
        for (CustomerCategory c : CustomerCategory.values()) {
            System.out.printf(" %-10s -> %s de descuento%n", c, porcentaje(c.getDiscountRate()));
        }
        System.out.println(raya('='));
        pausa();
    }

    private static void mostrarHistorial() {
        System.out.println();
        System.out.println(raya('='));
        System.out.println(centrar("HISTORIAL DE LA SESION"));
        System.out.println(raya('='));
        if (HISTORIAL.isEmpty()) {
            System.out.println(" Aun no hay ventas registradas en esta sesion.");
            System.out.println(raya('='));
            pausa();
            return;
        }
        double totFacturado = 0;
        double totAhorrado = 0;
        int i = 1;
        for (Registro r : HISTORIAL) {
            System.out.printf(Locale.US, " %d. %-14s %-8s Total: %10s  Ahorro: %10s%n",
                    i++, recortar(r.cliente(), 14), r.categoria(), money(r.total()), money(r.ahorro()));
            totFacturado += r.total();
            totAhorrado += r.ahorro();
        }
        System.out.println(raya('-'));
        System.out.printf(Locale.US, " Ventas: %d    Facturado: %s    Ahorrado: %s%n",
                HISTORIAL.size(), money(totFacturado), money(totAhorrado));
        System.out.println(raya('='));
        pausa();
    }

    // ====================== ENTRADA ROBUSTA ======================

    /** Lee una linea. Si el flujo de entrada se cierra (EOF), termina limpio. */
    private static String leerLinea() {
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("Entrada finalizada. Hasta luego.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    /** Lee un entero dentro de [min, max]; re-solicita ante cualquier error. */
    private static int leerEntero(String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            String s = leerLinea();
            try {
                int valor = Integer.parseInt(s);
                if (valor < min || valor > max) {
                    System.out.printf("  > Debe ser un numero entre %d y %d.%n", min, max);
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("  > Entrada invalida. Escribe solo numeros (ej. " + min + ").");
            }
        }
    }

    /** Lee 's' o 'n' de forma tolerante. */
    private static boolean leerSiNo(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            switch (leerLinea().toLowerCase(Locale.ROOT)) {
                case "s", "si", "sí", "y", "yes" -> { return true; }
                case "n", "no" -> { return false; }
                default -> System.out.println("  > Responde 's' (si) o 'n' (no).");
            }
        }
    }

    private static void pausa() {
        System.out.print("Presiona Enter para volver al menu...");
        leerLinea();
    }

    // ========================= UTILIDADES =========================

    private static Map<String, Coupon> crearCupones() {
        LinkedHashMap<String, Coupon> m = new LinkedHashMap<>();
        m.put("PROMO20", new Coupon("PROMO20", 20.0, null));
        m.put("DESC50", new Coupon("DESC50", 50.0, null));
        m.put("BIENVENIDA10", new Coupon("BIENVENIDA10", 10.0, null));
        m.put("VIEJO", new Coupon("VIEJO", 30.0, LocalDate.of(2020, 1, 1))); // vencido, para demo
        return m;
    }

    private static String money(double v) {
        return String.format(Locale.US, "$%.2f", v);
    }

    private static String porcentaje(double tasa) {
        return String.format(Locale.US, "%.0f%%", tasa * 100);
    }

    private static String raya(char c) {
        return String.valueOf(c).repeat(ANCHO);
    }

    private static String centrar(String texto) {
        if (texto.length() >= ANCHO) {
            return texto;
        }
        return " ".repeat((ANCHO - texto.length()) / 2) + texto;
    }

    private static String recortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max - 1) + ".";
    }

    private static void imprimirBienvenida() {
        System.out.println(raya('='));
        System.out.println(centrar("BIENVENIDO"));
        System.out.println(centrar("Sistema de venta de entradas"));
        System.out.println(raya('='));
    }

    private static void imprimirDespedida() {
        System.out.println();
        System.out.println(raya('='));
        System.out.println(centrar("Gracias por usar el sistema. Hasta pronto!"));
        System.out.println(raya('='));
    }
}