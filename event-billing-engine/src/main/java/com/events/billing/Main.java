package com.events.billing;

import com.events.billing.discount.Coupon;
import com.events.billing.discount.CouponDiscountRule;
import com.events.billing.discount.CustomerCategoryDiscountRule;
import com.events.billing.discount.DiscountRule;
import com.events.billing.exception.InvalidCouponException;
import com.events.billing.model.Customer;
import com.events.billing.model.CustomerCategory;
import com.events.billing.model.Event;
import com.events.billing.model.Purchase;
import com.events.billing.model.Ticket;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * Punto de entrada INTERACTIVO: permite al usuario registrar una compra
 * (categoria de cliente, tickets y cupon) y ver el total con descuento.
 *
 * Se ejecuta con:  gradlew run -q --console=plain
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Datos de referencia: un evento y el catalogo de cupones disponibles.
        Event evento = new Event("E1", "Concierto Rock", LocalDate.now().plusMonths(3), "concierto");
        Map<String, Coupon> cupones = Map.of(
                "PROMO20", new Coupon("PROMO20", 20.0, null),                  // -$20, no vence
                "DESC50",  new Coupon("DESC50", 50.0, null),                   // -$50, no vence
                "VIEJO",   new Coupon("VIEJO", 30.0, LocalDate.of(2020, 1, 1)) // vencido (para probar el error)
        );

        DiscountRule reglaCategoria = new CustomerCategoryDiscountRule();
        DiscountRule reglaCupon = new CouponDiscountRule(cupones);

        System.out.println("==============================");
        System.out.println("        NUEVA VENTA");
        System.out.println("==============================");
        System.out.println("Cupones: PROMO20 (-$20), DESC50 (-$50), VIEJO (vencido)");
        System.out.println();

        // 1) Categoria del cliente
        CustomerCategory categoria = leerCategoria();
        Customer cliente = new Customer("C1", "Cliente", categoria);

        // 2) Tickets
        int cantidad = leerEntero("Cuantos tickets? ", 1, 20);
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            double precio = leerDouble("  Precio del ticket " + i + ": $", 0);
            tickets.add(new Ticket("T" + i, evento, precio));
        }

        // 3) Cupon (opcional)
        System.out.print("Codigo de cupon (Enter para ninguno): ");
        String codigo = scanner.nextLine().trim();
        String codigoCupon = codigo.isBlank() ? null : codigo;

        // 4) Se arma la compra y se eligen las reglas a aplicar
        Purchase venta = new Purchase(cliente, tickets, codigoCupon, LocalDate.now());
        List<DiscountRule> reglas = new ArrayList<>();
        reglas.add(reglaCategoria);                 // siempre aplica la de categoria
        if (codigoCupon != null) {
            reglas.add(reglaCupon);                 // solo agrega la de cupon si el usuario ingreso uno
        }

        // 5) Calculo y resultado
        double subtotal = venta.getSubtotal();
        System.out.println();
        System.out.println("--------- RESUMEN ---------");
        System.out.printf("Cliente:  %s%n", categoria);
        System.out.printf("Tickets:  %d%n", cantidad);
        System.out.printf("Cupon:    %s%n", (codigoCupon == null ? "(ninguno)" : codigoCupon));
        System.out.printf(Locale.US, "Subtotal:      $%.2f%n", subtotal);

        try {
            double total = new DiscountEngine(reglas).calculateTotal(venta);
            System.out.printf(Locale.US, "Total a pagar: $%.2f%n", total);
            System.out.printf(Locale.US, "Ahorro:        $%.2f%n", subtotal - total);
        } catch (InvalidCouponException e) {
            // Si el cupon es invalido/vencido, se informa y se recalcula sin cupon.
            System.out.println("Cupon rechazado: " + e.getMessage());
            double total = new DiscountEngine(List.of(reglaCategoria)).calculateTotal(venta);
            System.out.printf(Locale.US, "Total (sin cupon): $%.2f%n", total);
            System.out.printf(Locale.US, "Ahorro:            $%.2f%n", subtotal - total);
        }
        System.out.println("---------------------------");
    }

    private static CustomerCategory leerCategoria() {
        while (true) {
            System.out.print("Categoria (1=REGULAR, 2=STUDENT, 3=SENIOR): ");
            switch (scanner.nextLine().trim()) {
                case "1": return CustomerCategory.REGULAR;
                case "2": return CustomerCategory.STUDENT;
                case "3": return CustomerCategory.SENIOR;
                default:  System.out.println("Opcion no valida, intenta de nuevo.");
            }
        }
    }

    private static int leerEntero(String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            try {
                int valor = Integer.parseInt(scanner.nextLine().trim());
                if (valor < min || valor > max) {
                    System.out.printf("Debe estar entre %d y %d.%n", min, max);
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un numero entero valido.");
            }
        }
    }

    private static double leerDouble(String mensaje, double min) {
        while (true) {
            System.out.print(mensaje);
            try {
                double valor = Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
                if (valor < min) {
                    System.out.printf(Locale.US, "Debe ser mayor o igual a %.2f.%n", min);
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un numero valido (ej. 50 o 50.5).");
            }
        }
    }
}