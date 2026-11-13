package com.paulrule.mathapi.domain;

/** BR-2: division by zero is rejected. */
public class DivisionByZeroException extends RuntimeException {

    public DivisionByZeroException() {
        super("Division by zero");
    }
}
