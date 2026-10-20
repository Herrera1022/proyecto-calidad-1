package com.events.billing;

import com.events.billing.console.ConsolaVentas;

import java.time.Clock;
import java.util.Scanner;

/**
 * Punto de entrada de la aplicacion de consola.
 *
 * Solo conecta la consola con la entrada/salida estandar y el reloj del
 * sistema; toda la logica de la interfaz esta en {@link ConsolaVentas}.
 *
 * Se ejecuta con:  gradlew run -q --console=plain
 */
public class Main {

    public static void main(String[] args) {
        new ConsolaVentas(new Scanner(System.in), System.out, Clock.systemDefaultZone()).iniciar();
    }
}
