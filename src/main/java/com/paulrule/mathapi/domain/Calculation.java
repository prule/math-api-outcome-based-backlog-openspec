package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
import java.math.MathContext;

/** One Operation applied to its Operands; {@code b} is null for a unary Operation (BR-9). */
public record Calculation(Operation operation, BigDecimal a, BigDecimal b) {

    /** BR-1 limits, on the Operand as written. */
    public static final int MAX_DIGITS = 100;

    public static final int MAX_SCALE = 1000;

    /** BR-3: how DIVIDE rounds (an enum's own static fields cannot be read from its constants). */
    public static final MathContext ROUNDING = MathContext.DECIMAL128;

    public Calculation {
        if ((b != null ? 2 : 1) != operation.arity()) {
            throw new ArityMismatchException(operation);
        }
        checkWithinLimits("a", a);
        if (b != null) {
            checkWithinLimits("b", b);
        }
    }

    private static void checkWithinLimits(String name, BigDecimal operand) {
        if (operand.precision() > MAX_DIGITS || operand.scale() < -MAX_SCALE || operand.scale() > MAX_SCALE) {
            throw new OperandOutOfRangeException(name);
        }
    }

    /** The Result, with trailing zeros stripped (BR-4). */
    public BigDecimal result() {
        return operation.apply(a, b).stripTrailingZeros();
    }
}
