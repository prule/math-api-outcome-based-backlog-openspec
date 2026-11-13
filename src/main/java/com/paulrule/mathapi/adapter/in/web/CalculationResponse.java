package com.paulrule.mathapi.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

/** All values are strings so clients cannot lose precision (PRD §3.2). */
record CalculationResponse(
        @Schema(description = "The Operation performed.") String operation,

        @Schema(description = "`a` as received, in plain notation with its given scale.")
        String a,

        @Schema(description = "`b` as received, in plain notation with its given scale.")
        String b,

        @Schema(description = "The Result: trailing zeros stripped, plain notation.")
        String result) {}
