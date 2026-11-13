package com.paulrule.mathapi.domain;

import java.math.BigDecimal;

/** One Operation applied to its Operands; {@code b} is null for a unary Operation (BR-9). */
public record Calculation(Operation operation, BigDecimal a, BigDecimal b) {

    public Calculation {
        if ((b != null ? 2 : 1) != operation.arity()) {
            throw new ArityMismatchException(operation);
        }
    }

    /** The Result, with trailing zeros stripped (BR-4). */
    public BigDecimal result() {
        return operation.apply(a, b).stripTrailingZeros();
    }
}
