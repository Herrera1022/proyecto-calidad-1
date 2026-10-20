package com.events.billing.console;

import com.events.billing.discount.Coupon;
import com.events.billing.model.Event;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Datos de demostracion de la consola: cartelera de eventos y cupones. */
final class Catalogo {

    private Catalogo() {
        // clase de utilidades: no se instancia
    }

    /** Cartelera con fechas relativas al dia de hoy. */
    static List<Espectaculo> cartelera(LocalDate hoy) {
        return List.of(
                new Espectaculo(new Event("EV1", "Concierto Rock", hoy.plusMonths(2), "concierto"), 120.0),
                new Espectaculo(new Event("EV2", "Conferencia Tech", hoy.plusMonths(1), "conferencia"), 80.0),
                new Espectaculo(new Event("EV3", "Cine Premier", hoy.plusWeeks(1), "cine"), 35.0)
        );
    }

    static Map<String, Coupon> cupones() {
        Map<String, Coupon> m = new LinkedHashMap<>();
        m.put("PROMO20", new Coupon("PROMO20", 20.0, null));
        m.put("DESC50", new Coupon("DESC50", 50.0, null));
        m.put("BIENVENIDA10", new Coupon("BIENVENIDA10", 10.0, null));
        m.put("VIEJO", new Coupon("VIEJO", 30.0, LocalDate.of(2020, 1, 1))); // vencido, para demo
        return m;
    }
}
