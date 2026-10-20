# Event Billing Engine

[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=Herrera1022_proyecto-calidad-1&metric=alert_status)](https://sonarcloud.io/project/overview?id=Herrera1022_proyecto-calidad-1)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Herrera1022_proyecto-calidad-1&metric=coverage)](https://sonarcloud.io/component_measures?id=Herrera1022_proyecto-calidad-1&metric=coverage)

Motor de facturación y descuentos para eventos (conciertos, conferencias, cine),
con una **aplicación de consola interactiva** para registrar ventas y una
**suite de pruebas** completa. Proyecto académico en **Java + Gradle (Kotlin DSL)**
enfocado en buenas prácticas de diseño y en calidad de software: pruebas unitarias
con el patrón AAA, pruebas de rendimiento, cobertura con **JaCoCo** y análisis de
código estático con **SonarQube Cloud**.

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

2. **Una aplicación de consola interactiva** (paquete `console`, arrancada desde
   `Main`) para usar el motor: registras una venta paso a paso (cliente, entradas
   de uno o varios eventos, cupón) y el sistema imprime la factura con el desglose
   de descuentos.

3. **Una suite de pruebas** que verifica la lógica, la consola y el modelo
   (pruebas unitarias con JUnit 5 y el patrón AAA) y su rendimiento (pruebas de
   performance).

---

## 2. Requisitos

- **JDK 21** (LTS). El *toolchain* de `build.gradle.kts` pide exactamente Java 21.
  Verifica con:
  ```
  java -version
  ```
- **Gradle 9.1.0**, que se descarga automáticamente con el *wrapper* incluido en el
  repositorio (`gradlew` / `gradlew.bat`). No necesitas instalar Gradle aparte.

> Nota de versiones: el proyecto se escribió originalmente con JDK 25; el toolchain
> se cambió a 21 (LTS) porque es el JDK instalado en el equipo del análisis local y
> el que usa el CI.

> Nota para Windows: si el proyecto está dentro de una carpeta sincronizada con
> **OneDrive**, Gradle puede fallar al limpiar `build/` (OneDrive convierte las
> carpetas en archivos "en la nube" de solo lectura). Clona el repositorio en una
> ruta local corta, por ejemplo `C:\dev\proyecto-calidad-1`.

---

## 3. Cómo ejecutar la aplicación interactiva

La forma recomendada es por **terminal**, desde la carpeta del proyecto:

```powershell
cd event-billing-engine
.\gradlew.bat run -q --console=plain
```

En Linux/macOS: `./gradlew run -q --console=plain`.

Detalles importantes de ese comando:
- `run` ejecuta la clase `Main` (definida en `build.gradle.kts`).
- `-q` (quiet) oculta los mensajes de progreso de Gradle para ver solo la app.
- `--console=plain` es **necesario** para que la lectura por teclado funcione
  correctamente.

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
Si la entrada se cierra (Ctrl+Z / Ctrl+D) la app termina con un mensaje en vez de
lanzar una excepción.

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

## 5. Cobertura local con JaCoCo

Al terminar `test`, Gradle genera automáticamente el reporte de **JaCoCo 0.8.13**
(`finalizedBy(tasks.jacocoTestReport)`). También se puede pedir explícitamente:

```powershell
.\gradlew.bat test jacocoTestReport
```

Rutas del reporte (dentro de `event-billing-engine/`):

| Formato | Ruta | Uso |
|---------|------|-----|
| HTML | `build/reports/jacoco/test/html/index.html` | Ver la cobertura en el navegador, por paquete, clase y línea |
| XML  | `build/reports/jacoco/test/jacocoTestReport.xml` | Lo importa SonarQube Cloud (`sonar.coverage.jacoco.xmlReportPaths`) |

```powershell
start build\reports\jacoco\test\html\index.html
```

En el HTML, verde = línea cubierta, amarillo = rama parcialmente cubierta,
rojo = no cubierta. La columna *Cxty* es la complejidad ciclomática por método.
No hay exclusiones de cobertura: se mide todo el código de producción.

---

## 6. Análisis estático con SonarQube Cloud

- Organización: `herrera1022`
- Proyecto: [`Herrera1022_proyecto-calidad-1`](https://sonarcloud.io/project/overview?id=Herrera1022_proyecto-calidad-1)
- Quality Profile: **Taller Calidad - Java** (hereda de *Sonar way comprehensive*)
- Quality Gate: **Taller Calidad Gate**
- *Automatic Analysis* desactivado: el análisis lo hace el plugin de Gradle
  `org.sonarqube`, para que SonarQube Cloud reciba el reporte de cobertura de JaCoCo.

### Desde local

El token **no** se guarda en el repositorio: el plugin lo lee de la variable de
entorno `SONAR_TOKEN` (se genera en SonarQube Cloud, *My Account → Security*).

```powershell
cd event-billing-engine
$env:SONAR_TOKEN = "<tu token>"        # o configurarla como variable de usuario
.\gradlew.bat build jacocoTestReport sonar
```

El plugin analiza `src/main/java` como código de producción y `src/test/java` como
código de pruebas (las reglas de tests también se aplican al código de pruebas).
La etiqueta de versión que se ve en la pestaña *Activity* sale de
`sonar.projectVersion` en `build.gradle.kts`.

### En CI (GitHub Actions)

El workflow [`.github/workflows/sonar.yml`](.github/workflows/sonar.yml) se ejecuta
en cada push a `main` y en cada pull request: JDK 21 (Temurin), compila, corre las
pruebas, genera JaCoCo y envía el análisis. El token se toma del secreto del
repositorio `SONAR_TOKEN` (*Settings → Secrets and variables → Actions*). Las
*actions* están fijadas por el SHA completo de su commit.

### Verificación de dependencias

`event-billing-engine/gradle/verification-metadata.xml` guarda el hash SHA-256 de
cada dependencia y plugin; si un artefacto descargado no coincide, el build falla.
Si se agrega o actualiza una dependencia, hay que regenerarlo **desde un clon limpio
y con una caché de Gradle vacía** (si no, faltan entradas que la caché local ya
tenía y el CI falla):

```bash
git clone https://github.com/Herrera1022/proyecto-calidad-1.git C:/dev/ci-test
cd C:/dev/ci-test/event-billing-engine
GRADLE_USER_HOME=C:/dev/ci-test/.guh ./gradlew --write-verification-metadata sha256 build jacocoTestReport
```

y luego copiar el `verification-metadata.xml` resultante al repositorio, revisar el
diff y hacer commit.

---

## 7. Estructura del proyecto

```
.github/workflows/sonar.yml          CI: build + pruebas + JaCoCo + SonarQube Cloud
event-billing-engine/
├── build.gradle.kts                 Config de Gradle (plugins, JaCoCo, Sonar, tareas)
├── settings.gradle.kts              Nombre del proyecto
├── gradle/
│   ├── verification-metadata.xml    Hashes SHA-256 de las dependencias
│   └── wrapper/                     Versión de Gradle
└── src/
    ├── main/java/com/events/billing/
    │   ├── model/                   Entidades del dominio
    │   │   ├── Event.java           Evento (id, nombre, fecha, categoría)
    │   │   ├── Ticket.java          Boleto con su precio base
    │   │   ├── Customer.java        Cliente y su categoría
    │   │   ├── CustomerCategory.java Enum de categorías y sus %
    │   │   └── Purchase.java        record: cliente, tickets, cupón; da el subtotal
    │   ├── discount/                Reglas de descuento
    │   │   ├── DiscountRule.java    Interfaz común (apply)
    │   │   ├── Coupon.java          record: código, monto, vencimiento
    │   │   ├── CouponDiscountRule.java           Descuento por cupón
    │   │   └── CustomerCategoryDiscountRule.java Descuento por categoría
    │   ├── exception/
    │   │   └── InvalidCouponException.java  Cupón inválido/vencido/nulo
    │   ├── console/                 Aplicación de consola (E/S y reloj inyectados)
    │   │   ├── ConsolaVentas.java   Menú, registro de ventas e historial
    │   │   ├── EntradaConsola.java  Lectura validada del teclado
    │   │   ├── Factura.java         record: datos e impresión de la factura
    │   │   ├── Formato.java         Dinero, porcentajes, centrado, rayas
    │   │   ├── Catalogo.java        Cartelera y cupones de prueba
    │   │   ├── Espectaculo.java     record: evento + precio en cartelera
    │   │   ├── ItemCarrito.java     record: espectáculo + cantidad
    │   │   └── FinDeEntradaException.java  Fin de la entrada estándar
    │   ├── DiscountEngine.java      Motor: encadena reglas y calcula el total
    │   └── Main.java                Punto de entrada: conecta la consola con System.in/out
    └── test/java/com/events/billing/
        ├── model/                   EventTest, CustomerTest, TicketTest, PurchaseTest
        ├── discount/                CouponTest, CouponDiscountRuleTest,
        │                            CustomerCategoryDiscountRuleTest
        ├── console/                 ConsolaVentasTest, EntradaConsolaTest,
        │                            FacturaTest, FormatoTest
        ├── DiscountEngineTest.java
        ├── MainTest.java
        └── performance/
            └── DiscountEnginePerformanceTest.java
```

La carpeta `build/` **no está en el repositorio**: la genera Gradle al compilar o
al correr los tests, y contiene los `.class`, los resultados XML y los reportes HTML
de pruebas y de cobertura.

---

## 8. Cómo funciona el motor de descuentos

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

## 9. Las pruebas (qué se verifica)

Todas las pruebas unitarias siguen el patrón **AAA** (Arrange–Act–Assert),
marcado con comentarios en el código, y usan **JUnit 5**. Son **120 pruebas**:

| Clase de prueba | Pruebas | Qué verifica |
|---|---:|---|
| `CustomerCategoryDiscountRuleTest` | 5 | 15%, 20%, 0%, categoría nula como REGULAR, montos pequeños sin errores de redondeo |
| `CouponDiscountRuleTest` | 10 | Cupón válido, descuento mayor que el total -> 0, cupón inexistente, vencido, nulo o vacío |
| `CouponTest` | 9 | Validaciones del record y vencimiento en el límite de la fecha |
| `DiscountEngineTest` | 5 | Una regla, ambas combinadas, ninguna, total no negativo |
| `EventTest`, `CustomerTest`, `TicketTest`, `PurchaseTest` | 44 | Validaciones de constructores, igualdad por id, inmutabilidad, `toString` |
| `ConsolaVentasTest` | 11 | Flujos completos de la consola con entrada simulada y fecha fija |
| `EntradaConsolaTest`, `FacturaTest`, `FormatoTest` | 33 | Lectura validada, impresión de la factura y formatos |
| `MainTest` | 1 | El punto de entrada conecta la consola con System.in/out |
| `DiscountEnginePerformanceTest` | 2 | 10.000 compras en menos de 1 s y 1 regla vs 2 reglas (aproximadas; no reemplazan JMH) |

---

## 10. Decisiones de diseño

- **Orden de las reglas combinadas**: el motor las aplica en el orden de la lista.
  En la app y en los tests combinados se aplica primero la de categoría (porcentaje)
  y luego la de cupón (monto fijo).
- **Categoría nula**: se trata como `REGULAR` (0%), no lanza excepción, para que el
  motor sea tolerante ante datos incompletos.
- **Cupón inválido/vencido**: la regla lanza `InvalidCouponException`; la app valida
  el cupón antes de aplicarlo y te deja reintentar o continuar sin cupón.
- **Consola con E/S inyectada**: `ConsolaVentas` recibe `Scanner`, `PrintStream` y
  `Clock` por constructor; así se prueba con entradas simuladas y una fecha fija.
- **Igualdad por id**: `Event`, `Customer` y `Ticket` son clases (no `record`) porque
  su `equals` compara solo el id; `Coupon` y `Purchase` sí son `record`.
- **Dinero como `double`**: se usa porque la interfaz `DiscountRule` lo define así;
  las comparaciones en los tests usan una tolerancia. En un sistema real de
  facturación se recomendaría `BigDecimal`.

---

## 11. Resumen de comandos

```powershell
.\gradlew.bat run -q --console=plain               # aplicación interactiva
.\gradlew.bat test                                 # pruebas + reporte JaCoCo
.\gradlew.bat test --rerun-tasks                   # re-ejecutar (si dice UP-TO-DATE)
start build\reports\tests\test\index.html          # reporte HTML de pruebas
start build\reports\jacoco\test\html\index.html    # reporte HTML de cobertura
.\gradlew.bat build jacocoTestReport sonar         # análisis (requiere SONAR_TOKEN)
.\gradlew.bat tasks                                # listar todas las tareas
```
