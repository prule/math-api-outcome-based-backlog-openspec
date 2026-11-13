package com.paulrule.mathapi.domain;

/** BR-9: an Operation takes exactly its arity of Operands. The message is safe to show clients. */
public class ArityMismatchException extends RuntimeException {

    public ArityMismatchException(Operation operation) {
        super(operation + " takes exactly " + operation.arity() + " operand(s).");
    }
}
