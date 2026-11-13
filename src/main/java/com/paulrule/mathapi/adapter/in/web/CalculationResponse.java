package com.paulrule.mathapi.adapter.in.web;

/** All values are strings so clients cannot lose precision (PRD §3.2). */
record CalculationResponse(String operation, String a, String b, String result) {}
