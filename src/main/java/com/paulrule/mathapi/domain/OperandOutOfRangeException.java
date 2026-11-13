package com.paulrule.mathapi.domain;

/** BR-1: an Operand is outside the limits. The message names the Operand, never its value, and is safe to show. */
public class OperandOutOfRangeException extends RuntimeException {

    public OperandOutOfRangeException(String operand) {
        super("Operand '" + operand + "' is out of range: it must have at most " + Calculation.MAX_DIGITS
                + " significant digits and a scale between -" + Calculation.MAX_SCALE + " and "
                + Calculation.MAX_SCALE + ".");
    }
}
