package com.paulrule.mathapi.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Operands arrive as JSON numbers or strings; Jackson reads both into BigDecimal without going through double. */
record CalculationRequest(
        @NotNull String operation, @NotNull BigDecimal a, BigDecimal b) {}
