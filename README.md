# Event Billing Engine

Motor de facturación y descuentos para eventos (conciertos, conferencias, cine),
con una **aplicación de consola interactiva** para registrar ventas y una
**suite de pruebas** completa. Proyecto académico en **Java + Gradle (Kotlin DSL)**
enfocado en buenas prácticas de diseño y en calidad de software (pruebas unitarias
con el patrón AAA y pruebas de rendimiento).

---

## 1. ¿Qué hace el proyecto?

El sistema calcula el total a pagar de una compra de entradas aplicando reglas de
descuento. Tiene tres partes:

1. **El dominio y el motor de descuentos** (la lógica). Modela eventos, tickets,
   clientes y compras, y aplica dos reglas de descuento:
   - **Por cupón**: un código de monto fijo (ej. `PROMO20` descuenta $20). Valida
     que el cupón exista y no esté vencido.
   - **Por categoría del cliente**: `STUDENT` 15%, `SENIOR` 20%, `REGULAR` 0%.

   El motor (`DiscountEngine`) puede aplicar una regla, ambas combinadas o
   ninguna, y garantiza que el total nunca sea negativo.

2. **Una aplicación de consola interactiva** (`Main`) para usar el motor: registras
   una venta paso a paso (cliente, entradas de uno o varios eventos, cupón) y el
   sistema imprime la factura con el desglose de descuentos.

3. **Una suite de pruebas** que verifica la lógica (pruebas unitarias con JUnit 5
   y el patrón AAA) y su rendimiento (pruebas de performance).

---

## 2. Requisitos

- **JDK 17 o superior** (el proyecto se probó con JDK 25). Verifica con:
  ```
  java -version
  ```
- **Gradle 9.1.0**, que se obtiene automáticamente a través del *wrapper*
  (`gradle/wrapper/gradle-wrapper.properties`). No necesitas instalar Gradle
  aparte si usas VS Code o generas el wrapper (ver sección 3).

> Nota de versiones: se usa Gradle 9.1.0 porque es la versión que soporta Java 25.
> Con un JDK más antiguo (17 o 21) funciona igual.

---

## 3. Cómo ejecutar la aplicación interactiva

La forma recomendada es por **terminal**, desde la carpeta del proyecto:

```powershell
cd event-billing-engine
.\gradlew.bat run -q --console=plain
```

Detalles importantes de ese comando:
- `run` ejecuta la clase `Main` (definida en `build.gradle.kts`).
- `-q` (quiet) oculta los mensajes de progreso de Gradle para ver solo la app.
- `--console=plain` es **necesario** para que la lectura por teclado funcione
  correctamente.

### Si no tienes el archivo `gradlew.bat`

El binario del wrapper no viene incluido. Para generarlo, la opción más simple es:
- Abrir el proyecto en **VS Code** (con el *Extension Pack for Java*) y usar el
  panel de Gradle (ícono del elefante) -> `event-billing-engine` -> **Tasks** ->
  **build setup** -> doble clic en **wrapper**. Eso crea `gradlew.bat`.
- O, si tienes Gradle instalado en el sistema, ejecutar `gradle wrapper` una vez,
  o directamente `gradle run -q --console=plain`.

### Qué verás al ejecutarla

La app muestra un menú y te guía paso a paso:

```
======================================================
                      BIENVENIDO
             Sistema de venta de entradas
======================================================

======================================================
            MOTOR DE FACTURACION - EVENTOS
======================================================
  1) Registrar una venta
  2) Ver cupones disponibles
  3) Ver tarifas por categoria
  4) Historial de la sesion
  5) Salir
------------------------------------------------------
Selecciona una opcion (1-5):
```

Al **registrar una venta** te pide: nombre del cliente, categoría, las entradas
(puedes agregar varios eventos al carrito) y un cupón opcional. Al final imprime
una factura con el desglose:

```
======================================================
                   FACTURA DE VENTA
======================================================
 Cliente   : Ana Gomez
 Categoria : STUDENT (15% de descuento)
 Fecha     : 2026-08-18
 Cupon     : PROMO20
------------------------------------------------------
 DETALLE
   2 x Concierto Rock          $240.00
   1 x Cine Premier             $35.00
------------------------------------------------------
 Subtotal                      $275.00
 Descuento categoria           -$41.25
 Descuento cupon               -$20.00
------------------------------------------------------
 TOTAL A PAGAR                 $213.75
 Ahorro total                   $61.25
======================================================
```

### Cupones de prueba

| Código         | Descuento | Estado   |
|----------------|-----------|----------|
| `PROMO20`      | -$20      | vigente  |
| `DESC50`       | -$50      | vigente  |
| `BIENVENIDA10` | -$10      | vigente  |
| `VIEJO`        | -$30      | vencido  |

La app **maneja errores de digitación**: si escribes letras donde va un número,
una opción fuera de rango o un cupón inexistente, te muestra un mensaje y vuelve a
preguntar en vez de fallar. Los cupones no distinguen mayúsculas (`promo20` = `PROMO20`).

---

## 4. Cómo ejecutar las pruebas

Desde la carpeta del proyecto:

```powershell
.\gradlew.bat test
```

Si dice `UP-TO-DATE` porque ya las corriste sin cambios, fuerza la re-ejecución:

```powershell
.\gradlew.bat test --rerun-tasks
```

También puedes correrlas desde VS Code en la **vista Testing** (ícono de matraz) o
en el panel de Gradle -> **Tasks** -> **verification** -> **test**.

### Ver el reporte de resultados

Gradle genera un reporte HTML al terminar. Ábrelo con:

```powershell
start build\reports\tests\test\index.html
```

Ahí ves la tasa de éxito, la duración, el detalle test por test, y la salida de
consola de los performance tests (líneas `[perf] ...`).

---

## 5. Estructura del proyecto

```
event-billing-engine/
├── build.gradle.kts                 Config de Gradle (plugins, dependencias, tareas)
├── settings.gradle.kts              Nombre del proyecto
├── gradle/wrapper/                  Versión de Gradle
├── README.md                        Este archivo
└── src/
    ├── main/java/com/events/billing/
    │   ├── model/                   Entidades del dominio
    │   │   ├── Event.java           Evento (id, nombre, fecha, categoría)
    │   │   ├── Ticket.java          Boleto con su precio base
    │   │   ├── Customer.java        Cliente y su categoría
    │   │   ├── CustomerCategory.java Enum de categorías y sus %
    │   │   └── Purchase.java        Compra: cliente, tickets, cupón; da el subtotal
    │   ├── discount/                Reglas de descuento
    │   │   ├── DiscountRule.java    Interfaz común (apply)
    │   │   ├── Coupon.java          Cupón: código, monto, vencimiento
    │   │   ├── CouponDiscountRule.java           Descuento por cupón
    │   │   └── CustomerCategoryDiscountRule.java Descuento por categoría
    │   ├── exception/
    │   │   └── InvalidCouponException.java  Cupón inválido/vencido/nulo
    │   ├── DiscountEngine.java      Motor: encadena reglas y calcula el total
    │   └── Main.java                Aplicación de consola interactiva
    └── test/java/com/events/billing/
        ├── discount/
        │   ├── CouponDiscountRuleTest.java
        │   └── CustomerCategoryDiscountRuleTest.java
        ├── DiscountEngineTest.java
        └── performance/
            └── DiscountEnginePerformanceTest.java
```

La carpeta `build/` **no está en el repositorio**: la genera Gradle al compilar o
al correr los tests, y contiene los `.class`, los resultados XML y el reporte HTML.

---

## 6. Cómo funciona el motor de descuentos

`DiscountEngine.calculateTotal(purchase)` hace tres cosas:

1. Calcula el **subtotal** (suma de precios de los tickets).
2. Aplica las reglas de descuento **secuencialmente, en el orden de la lista** con
   que se construyó el motor: la salida de una regla es la entrada de la siguiente.
3. Garantiza que el total final **nunca sea negativo** (mínimo 0).

Ejemplo (subtotal $100, cliente estudiante, cupón de $20, orden categoría -> cupón):
`100 -> (−15%) 85 -> (−$20) 65`.

Las reglas implementan la interfaz `DiscountRule` con `apply(double, Purchase)`, lo
que permite combinarlas, usarlas por separado o no usar ninguna.

---

## 7. Las pruebas (qué se verifica)

Todas las pruebas unitarias siguen el patrón **AAA** (Arrange–Act–Assert),
marcado con comentarios en el código, y usan **JUnit 5**.

- `CustomerCategoryDiscountRuleTest`: la regla de categoría (15%, 20%, 0%, categoría
  nula tratada como REGULAR, y montos pequeños sin errores de redondeo).
- `CouponDiscountRuleTest`: la regla de cupón (cupón válido, descuento mayor que el
  total -> 0, y errores: inexistente, vencido, nulo, vacío). Incluye un test
  parametrizado con `@CsvSource`.
- `DiscountEngineTest`: el motor completo (una regla, ambas combinadas, ninguna, y
  la garantía de total no negativo).
- `DiscountEnginePerformanceTest`: pruebas de rendimiento con `System.nanoTime()`
  (procesar 10.000 compras en menos de 1 segundo y comparar 1 regla vs 2 reglas).
  Son aproximadas; no reemplazan un benchmark riguroso como JMH.

---

## 8. Decisiones de diseño

- **Orden de las reglas combinadas**: el motor las aplica en el orden de la lista.
  En la app y en los tests combinados se aplica primero la de categoría (porcentaje)
  y luego la de cupón (monto fijo).
- **Categoría nula**: se trata como `REGULAR` (0%), no lanza excepción, para que el
  motor sea tolerante ante datos incompletos.
- **Cupón inválido/vencido**: la regla lanza `InvalidCouponException`; la app la
  captura y te deja reintentar o continuar sin cupón.
- **Dinero como `double`**: se usa porque la interfaz `DiscountRule` lo define así;
  las comparaciones en los tests usan una tolerancia. En un sistema real de
  facturación se recomendaría `BigDecimal`.

---

## 9. Resumen de comandos

```powershell
.\gradlew.bat run -q --console=plain          # aplicación interactiva
.\gradlew.bat test                            # correr las pruebas
.\gradlew.bat test --rerun-tasks              # re-ejecutar (si dice UP-TO-DATE)
start build\reports\tests\test\index.html     # abrir el reporte HTML
.\gradlew.bat build                           # compilar + probar + empaquetar
.\gradlew.bat tasks                           # listar todas las tareas
```
