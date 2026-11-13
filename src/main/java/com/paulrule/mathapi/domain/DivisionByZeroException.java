package com.paulrule.mathapi.domain;

/** BR-2: division by zero is rejected. The message is safe to show clients. */
public class DivisionByZeroException extends RuntimeException {

    public DivisionByZeroException() {
        super("Cannot divide by zero.");
    }
}
