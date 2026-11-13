package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
import java.util.function.BinaryOperator;

/** The closed set of arithmetic functions (DOMAIN-MODEL.md §3). */
public enum Operation {
    ADD(2, BigDecimal::add),
    SUBTRACT(2, BigDecimal::subtract),
    MULTIPLY(2, BigDecimal::multiply),
    DIVIDE(2, (a, b) -> {
        if (b.signum() == 0) { // BR-2: every zero representation
            throw new DivisionByZeroException();
        }
        return a.divide(b, Calculation.ROUNDING);
    });

    private final int arity;
    private final BinaryOperator<BigDecimal> function;

    Operation(int arity, BinaryOperator<BigDecimal> function) {
        this.arity = arity;
        this.function = function;
    }

    public int arity() {
        return arity;
    }

    /** Applies the function to its Operands; {@code b} is null for a unary Operation. */
    public BigDecimal apply(BigDecimal a, BigDecimal b) {
        return function.apply(a, b);
    }

    /** Exact, case-sensitive lookup. */
    public static Operation named(String name) {
        if (name == null) {
            throw new UnknownOperationException();
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new UnknownOperationException();
        }
    }
}
