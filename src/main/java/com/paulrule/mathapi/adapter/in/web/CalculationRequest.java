package com.paulrule.mathapi.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Operands arrive as JSON numbers or strings; Jackson reads both into BigDecimal without going through double. */
record CalculationRequest(
        @Schema(description = "The Operation to perform; exact, case-sensitive name.") @NotNull
        String operation,

        @Schema(
                types = {"number", "string"},
                description = "First Operand: a decimal number, as a JSON number or string.")
        @NotNull
        BigDecimal a,

        @Schema(
                types = {"number", "string"},
                description = "Second Operand, as a JSON number or string. Required by every binary Operation.")
        BigDecimal b) {}
