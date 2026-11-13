package com.paulrule.mathapi.adapter.in.web;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

/** All values are strings so clients cannot lose precision (PRD §3.2). */
record CalculationResponse(
        @Schema(requiredMode = REQUIRED, description = "The Operation performed.")
        String operation,

        @Schema(requiredMode = REQUIRED, description = "`a` as received, in plain notation with its given scale.")
        String a,

        @Schema(requiredMode = REQUIRED, description = "`b` as received, in plain notation with its given scale.")
        String b,

        @Schema(requiredMode = REQUIRED, description = "The Result: trailing zeros stripped, plain notation.")
        String result) {}
