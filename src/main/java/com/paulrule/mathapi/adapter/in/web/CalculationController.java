package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.application.PerformCalculation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calculations")
class CalculationController {

    private final PerformCalculation performCalculation;

    CalculationController(PerformCalculation performCalculation) {
        this.performCalculation = performCalculation;
    }

    @PostMapping
    CalculationResponse calculate(@Valid @RequestBody CalculationRequest request) {
        var calculation = performCalculation.perform(request.operation(), request.a(), request.b());
        // Operands keep the scale they were sent with; only the Result is normalised (BR-4).
        return new CalculationResponse(
                calculation.operation().name(),
                calculation.a().toPlainString(),
                calculation.b().toPlainString(),
                calculation.result().toPlainString());
    }
}
