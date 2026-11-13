package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.application.PerformCalculation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
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

    /*
     * Each request example has a response example of the same name: what the API returns for it.
     * ApiDocumentationTest replays every pair. The unknown-operation example's detail is generated in OpenApiConfig.
     */
    @Operation(
            summary = "Perform one Calculation",
            description = "Applies one Operation to its Operands and returns the exact Result, rounded only where"
                    + " the Operation's rules say (BR-3). Only the first failure is reported (PRD §3.2 order).",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                    @Content(
                                            mediaType = "application/json",
                                            examples = {
                                                @ExampleObject(
                                                        name = "numbers",
                                                        summary = "Operands as JSON numbers",
                                                        value = "{\"operation\": \"ADD\", \"a\": 0.1, \"b\": 0.2}"),
                                                @ExampleObject(
                                                        name = "strings",
                                                        summary = "Operands as JSON strings",
                                                        value =
                                                                "{\"operation\": \"DIVIDE\", \"a\": \"1\", \"b\": \"3\"}"),
                                                @ExampleObject(
                                                        name = "invalid-request",
                                                        summary = "A binary Operation without b (BR-9)",
                                                        value = "{\"operation\": \"ADD\", \"a\": 1}"),
                                                @ExampleObject(
                                                        name = "unknown-operation",
                                                        summary = "A name that is not an Operation",
                                                        value =
                                                                "{\"operation\": \"NOT_AN_OPERATION\", \"a\": 1, \"b\": 2}"),
                                                @ExampleObject(
                                                        name = "operand-out-of-range",
                                                        summary = "An Operand outside BR-1",
                                                        value =
                                                                "{\"operation\": \"MULTIPLY\", \"a\": \"1e999999999\", \"b\": 2}"),
                                                @ExampleObject(
                                                        name = "division-by-zero",
                                                        summary = "Dividing by zero (BR-2)",
                                                        value = "{\"operation\": \"DIVIDE\", \"a\": 1, \"b\": 0}")
                                            })),
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "The Result. Every value is a string, so no precision is lost.",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = CalculationResponse.class),
                                        examples = {
                                            @ExampleObject(
                                                    name = "numbers",
                                                    value = "{\"operation\": \"ADD\", \"a\": \"0.1\", \"b\": \"0.2\","
                                                            + " \"result\": \"0.3\"}"),
                                            @ExampleObject(
                                                    name = "strings",
                                                    value = "{\"operation\": \"DIVIDE\", \"a\": \"1\", \"b\": \"3\","
                                                            + " \"result\": \"0.3333333333333333333333333333333333\"}")
                                        })),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request, unknown operation, or an Operand outside BR-1.",
                        content =
                                @Content(
                                        mediaType = "application/problem+json",
                                        schema = @Schema(implementation = ProblemDetail.class),
                                        examples = {
                                            @ExampleObject(
                                                    name = "invalid-request",
                                                    value = "{\"type\": \"/problems/invalid-request\", \"title\":"
                                                            + " \"Invalid request\", \"status\": 400, \"detail\":"
                                                            + " \"ADD takes exactly 2 operand(s).\", \"instance\":"
                                                            + " \"/api/v1/calculations\"}"),
                                            @ExampleObject(name = "unknown-operation"),
                                            @ExampleObject(
                                                    name = "operand-out-of-range",
                                                    value = "{\"type\": \"/problems/operand-out-of-range\", \"title\":"
                                                            + " \"Operand out of range\", \"status\": 400, \"detail\":"
                                                            + " \"Operand 'a' is out of range: it must have at most 100"
                                                            + " significant digits and a scale between -1000 and 1000.\","
                                                            + " \"instance\": \"/api/v1/calculations\"}")
                                        })),
                @ApiResponse(
                        responseCode = "422",
                        description = "A valid Calculation that breaks a domain rule.",
                        content =
                                @Content(
                                        mediaType = "application/problem+json",
                                        schema = @Schema(implementation = ProblemDetail.class),
                                        examples = {
                                            @ExampleObject(
                                                    name = "division-by-zero",
                                                    value = "{\"type\": \"/problems/division-by-zero\", \"title\":"
                                                            + " \"Division by zero\", \"status\": 422, \"detail\":"
                                                            + " \"Cannot divide by zero.\", \"instance\":"
                                                            + " \"/api/v1/calculations\"}")
                                        }))
            })
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
