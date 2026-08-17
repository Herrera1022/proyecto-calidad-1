# Event Billing Engine

Motor de facturación y descuentos para eventos (conciertos, conferencias, cine).
Proyecto académico en **Java 17 + Gradle (Kotlin DSL)** que demuestra:

1. Diseño OO con interfaces e inyección de dependencias simple.
2. Pruebas unitarias con el patrón **AAA (Arrange–Act–Assert)** usando **JUnit 5**.
3. Pruebas de **rendimiento** (performance testing) aproximadas con `System.nanoTime()`.

---

## Requisitos

- **JDK 17** o superior (`java -version`).
- **Gradle 8.x** — ver la nota sobre el *wrapper* más abajo.

---

## Cómo ejecutar los tests

Desde la carpeta `event-billing-engine/`:

```bash
./gradlew test        # si tienes el wrapper generado
# o, si tienes Gradle instalado en el sistema:
gradle test
```

Al terminar, el reporte HTML queda en:

```
build/reports/tests/test/index.html
```

Los **performance tests** imprimen sus tiempos en la consola (por eso el build
activa `showStandardStreams = true`).

### Nota sobre el Gradle Wrapper

Este paquete incluye `gradle/wrapper/gradle-wrapper.properties` (fija Gradle
8.10.2), pero **no** el binario `gradle-wrapper.jar`. Para obtener el wrapper
completo tienes dos opciones:

- **Recomendado:** abre la carpeta en **IntelliJ IDEA**. El IDE detecta el
  proyecto Gradle y genera el wrapper automáticamente; luego puedes correr los
  tests desde la interfaz o la terminal integrada.
- **Con Gradle instalado:** ejecuta una vez `gradle wrapper` en la carpeta del
  proyecto; eso crea `gradlew`, `gradlew.bat` y el `.jar`. Después ya puedes
  usar `./gradlew test`.

---

## Estructura

```
event-billing-engine/
 ├── build.gradle.kts / settings.gradle.kts   Configuración de Gradle
 └── src/
      ├── main/java/com/events/billing/
      │     ├── model/       Event, Ticket, Customer, CustomerCategory, Purchase
      │     ├── discount/    DiscountRule, Coupon, CouponDiscountRule,
      │     │                CustomerCategoryDiscountRule
      │     ├── exception/   InvalidCouponException
      │     └── DiscountEngine.java
      └── test/java/com/events/billing/
            ├── discount/    Tests de cada regla
            ├── DiscountEngineTest.java
            └── performance/ DiscountEnginePerformanceTest.java
```

---

## Cómo funciona el motor

`DiscountEngine.calculateTotal(purchase)`:

1. Calcula el **subtotal** (suma de precios de los tickets).
2. Aplica las reglas de descuento **secuencialmente, en el orden de la lista**
   con que se construyó el motor (la salida de una regla es la entrada de la
   siguiente).
3. Garantiza que el total final **nunca sea negativo** (mínimo 0).

Las reglas implementan la interfaz `DiscountRule` con `apply(double, Purchase)`,
lo que permite usarlas solas, combinadas o ninguna.

---

## Decisiones de diseño (documentadas también en el código)

Donde el enunciado dejaba libertad, se tomaron estas decisiones. Cada una está
comentada en el archivo correspondiente para poder ajustarla:

- **Orden de las reglas combinadas:** el motor las aplica en el orden de la
  lista recibida. En los tests combinados se usa primero la de **categoría**
  (porcentaje) y luego la de **cupón** (monto fijo). Ej.: subtotal 100 →
  estudiante 15% → 85 → cupón $20 → 65.
- **Categoría de cliente nula:** `CustomerCategoryDiscountRule` la trata como
  `REGULAR` (0% de descuento) en vez de lanzar excepción, para que el motor sea
  tolerante ante datos incompletos.
- **Cupón:** `CouponDiscountRule` se construye con un repositorio (`Map` de
  cupones válidos) y toma el código desde `Purchase`. Lanza
  `InvalidCouponException` si el código es nulo/vacío, no existe o está vencido.
  Si el descuento supera el total, el resultado queda en 0 (no negativo).
- **Dinero como `double`:** se usa `double` porque la interfaz `DiscountRule`
  lo exige. Las comparaciones en los tests usan una tolerancia (`delta`). En un
  sistema real de facturación se recomendaría `BigDecimal`.

---

## Sobre los performance tests

Son una **prueba de humo** de rendimiento con `System.nanoTime()`:

- Procesar **10.000 compras** aleatorias (semilla fija = reproducible) debe
  tomar **menos de 1 segundo**.
- Comparación de tiempo con **1 regla vs 2 reglas** para observar que el costo
  crece de forma **lineal**, no exponencial.

Estas mediciones son **aproximadas** y no sustituyen un benchmark riguroso con
**JMH** (que hace calentamiento de la JVM, múltiples *forks*, etc.).
