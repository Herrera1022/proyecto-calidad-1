package com.events.billing.exception;

/**
 * Se lanza cuando un cupon no es valido: no existe, esta vencido, o su codigo
 * es nulo o vacio. Es una excepcion no chequeada (RuntimeException) para no
 * obligar a capturarla en toda la cadena de llamadas.
 */
public class InvalidCouponException extends RuntimeException {

    public InvalidCouponException(String message) {
        super(message);
    }
}
