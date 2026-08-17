package com.events.billing.performance;

import com.events.billing.DiscountEngine;
import com.events.billing.discount.Coupon;
import com.events.billing.discount.CouponDiscountRule;
import com.events.billing.discount.CustomerCategoryDiscountRule;
import com.events.billing.discount.DiscountRule;
import com.events.billing.model.Customer;
import com.events.billing.model.CustomerCategory;
import com.events.billing.model.Event;
import com.events.billing.model.Purchase;
import com.events.billing.model.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de RENDIMIENTO (performance testing) del motor.
 *
 * IMPORTANTE: estas mediciones con System.nanoTime() son APROXIMADAS y sirven
 * como "prueba de humo" de rendimiento. NO reemplazan un benchmark riguroso
 * como JMH (que hace calentamiento de la JVM, multiples forks, etc.). El
 * objetivo aqui es solo verificar que el motor procesa un volumen alto en un
 * tiempo razonable y que el costo crece de forma lineal (no exponencial) al
 * anadir reglas.
 */
@DisplayName("DiscountEngine - performance")
class DiscountEnginePerformanceTest {

    private static final int CANTIDAD = 10_000;
    private static final long SEMILLA = 42L; // semilla fija -> datos reproducibles

    // Cupon valido y sin vencimiento, usado por todas las compras generadas
    // para que la regla de cupon nunca lance excepcion durante la medicion.
    private static final Map<String, Coupon> REPO_CUPONES =
            Map.of("PERF", new Coupon("PERF", 5.0, null));

    private final Event evento = new Event("E1", "Evento", LocalDate.of(2026, 12, 1), "concierto");

    /** Genera una lista de compras aleatorias pero reproducibles. */
    private List<Purchase> generarCompras(int cantidad) {
        Random random = new Random(SEMILLA);
        CustomerCategory[] categorias = CustomerCategory.values();
        List<Purchase> compras = new ArrayList<>(cantidad);

        for (int i = 0; i < cantidad; i++) {
            CustomerCategory categoria = categorias[random.nextInt(categorias.length)];
            Customer cliente = new Customer("C" + i, "Cliente" + i, categoria);

            int numTickets = 1 + random.nextInt(5); // entre 1 y 5 tickets
            List<Ticket> tickets = new ArrayList<>(numTickets);
            for (int j = 0; j < numTickets; j++) {
                double precio = 10 + random.nextInt(191); // entre 10 y 200
                tickets.add(new Ticket("T" + i + "_" + j, evento, precio));
            }

            compras.add(new Purchase(cliente, tickets, "PERF", LocalDate.of(2026, 6, 1)));
        }
        return compras;
    }

    /** Recorre todas las compras calculando su total y devuelve el tiempo en ns. */
    private long medirProceso(DiscountEngine motor, List<Purchase> compras) {
        long inicio = System.nanoTime();
        double acumulado = 0.0;
        for (Purchase compra : compras) {
            acumulado += motor.calculateTotal(compra); // se acumula para que el JIT no lo elimine
        }
        long fin = System.nanoTime();
        // uso trivial del acumulado para evitar que se optimice el bucle entero
        if (acumulado < 0) {
            throw new IllegalStateException("nunca deberia ocurrir");
        }
        return fin - inicio;
    }

    @Test
    @DisplayName("deberia procesar 10.000 compras en menos de 1 segundo")
    void deberiaProcesar10000ComprasEnMenosDeUnSegundo() {
        // Arrange
        List<DiscountRule> reglas = List.of(new CustomerCategoryDiscountRule(),
                                            new CouponDiscountRule(REPO_CUPONES));
        DiscountEngine motor = new DiscountEngine(reglas);
        List<Purchase> compras = generarCompras(CANTIDAD);

        // Act
        long nanos = medirProceso(motor, compras);
        long millis = nanos / 1_000_000;

        // Assert
        long umbralMillis = 1000; // 1 segundo
        System.out.printf("[perf] Procesar %d compras tomo %d ms%n", CANTIDAD, millis);
        assertTrue(millis < umbralMillis,
                "Procesar " + CANTIDAD + " compras tardo " + millis
                        + " ms, supera el umbral de " + umbralMillis + " ms");
    }

    @Test
    @DisplayName("deberia crecer de forma lineal al pasar de 1 regla a 2 reglas")
    void deberiaCrecerLinealmenteAlCompararUnaReglaVsDosReglas() {
        // Arrange
        List<Purchase> compras = generarCompras(CANTIDAD);
        DiscountEngine motorUnaRegla = new DiscountEngine(
                List.of(new CustomerCategoryDiscountRule()));
        DiscountEngine motorDosReglas = new DiscountEngine(
                List.of(new CustomerCategoryDiscountRule(), new CouponDiscountRule(REPO_CUPONES)));

        // Act -> una ronda de calentamiento antes de medir da lecturas mas estables
        medirProceso(motorUnaRegla, compras);
        medirProceso(motorDosReglas, compras);
        long tiempoUnaRegla = medirProceso(motorUnaRegla, compras);
        long tiempoDosReglas = medirProceso(motorDosReglas, compras);

        // Assert / observacion -> se imprimen ambos tiempos para comparar
        System.out.printf("[perf] 1 regla : %d us%n", tiempoUnaRegla / 1_000);
        System.out.printf("[perf] 2 reglas: %d us%n", tiempoDosReglas / 1_000);
        System.out.printf("[perf] factor  : %.2fx%n",
                (double) tiempoDosReglas / Math.max(1, tiempoUnaRegla));

        // Comprobacion suave: con 2 reglas no deberia tardar mas de ~5x que con 1.
        // (Un crecimiento exponencial dispararia este factor muy por encima.)
        assertTrue(tiempoDosReglas < tiempoUnaRegla * 5L + 5_000_000L,
                "El costo con 2 reglas crecio mucho mas que linealmente");
    }
}
