package com.paulrule.mathapi.application;

import com.paulrule.mathapi.domain.Calculation;
import com.paulrule.mathapi.domain.Operation;
import java.math.BigDecimal;

/** Use case: perform one Calculation from an Operation name and its Operands. */
public class PerformCalculation {

    /** {@code b} is null when absent. Lookup (unknown operation) runs before the arity check (BR-9). */
    public Calculation perform(String operation, BigDecimal a, BigDecimal b) {
        return new Calculation(Operation.named(operation), a, b);
    }
}
