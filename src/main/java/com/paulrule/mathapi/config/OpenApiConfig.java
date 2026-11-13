package com.paulrule.mathapi.config;

import com.paulrule.mathapi.adapter.in.web.RequestBodyLimit;
import com.paulrule.mathapi.domain.ArityMismatchException;
import com.paulrule.mathapi.domain.Calculation;
import com.paulrule.mathapi.domain.DivisionByZeroException;
import com.paulrule.mathapi.domain.OperandOutOfRangeException;
import com.paulrule.mathapi.domain.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documented values that come from the domain (and the request limits) are generated here, never written in
 * annotations (STANDARDS §4). Each Problem Details example is built from the message the API itself sends.
 */
@Configuration
public class OpenApiConfig {

    private static final String PATH = "/api/v1/calculations";
    private static final String PROBLEM_JSON = "application/problem+json";

    private static final List<String> OPERATIONS =
            Arrays.stream(Operation.values()).map(Operation::name).toList();

    @Bean
    OpenApiCustomizer apiInfo() {
        return openApi -> openApi.info(new Info()
                .title("Math API")
                .version("v1")
                .description("Exact decimal arithmetic. Every error is RFC 9457 Problem Details."));
    }

    @Bean
    OpenApiCustomizer domainValues() {
        return openApi -> {
            var post = openApi.getPaths().get(PATH).getPost();
            post.setDescription(description());
            schemaProperty(openApi, "CalculationRequest", "operation").setEnum(OPERATIONS);
            describeProblemDetailAsSent(openApi);

            // Each example answers the request example of the same name on CalculationController.
            var badRequest = examples(post.getResponses().get("400").getContent());
            badRequest.put(
                    "invalid-request",
                    problem(
                            "invalid-request",
                            "Invalid request",
                            400,
                            new ArityMismatchException(Operation.ADD).getMessage()));
            badRequest.put(
                    "unknown-operation",
                    problem(
                            "unknown-operation",
                            "Unknown operation",
                            400,
                            "Unknown operation. Supported operations: " + String.join(", ", OPERATIONS) + "."));
            badRequest.put(
                    "operand-out-of-range",
                    problem(
                            "operand-out-of-range",
                            "Operand out of range",
                            400,
                            new OperandOutOfRangeException("a").getMessage()));
            examples(post.getResponses().get("422").getContent())
                    .put(
                            "division-by-zero",
                            problem(
                                    "division-by-zero",
                                    "Division by zero",
                                    422,
                                    new DivisionByZeroException().getMessage()));
        };
    }

    /** The rules a client needs, stated plainly, with every number taken from the code that enforces it. */
    private static String description() {
        return String.format(
                Locale.ROOT,
                """
                Applies one Operation to its Operands and returns the Result. Every value in the response is a \
                string, so no precision is lost.

                - **Operations**: %s. ADD, SUBTRACT and MULTIPLY are exact. DIVIDE rounds to %d significant digits, \
                %s (a tie goes to the even digit).
                - **Result**: trailing zeros stripped, plain notation (never `1E+3`). `a` and `b` are echoed as \
                received.
                - **Operands**: a JSON number or a JSON string, at most %d significant digits as written and a \
                scale between -%d and %d.
                - **Request limits**: body at most %,d characters; any JSON string or number at most %,d \
                characters; nesting at most %d levels.
                - **Failures**: only the first is reported, in this order: invalid request, unknown operation, \
                operand count, operand out of range, division by zero.
                - **Other HTTP errors** (404, 405, 415, 500) are Problem Details too, with `type` `about:blank`.
                """,
                String.join(", ", OPERATIONS),
                Calculation.ROUNDING.getPrecision(),
                Calculation.ROUNDING.getRoundingMode(),
                Calculation.MAX_DIGITS,
                Calculation.MAX_SCALE,
                Calculation.MAX_SCALE,
                RequestBodyLimit.MAX_CHARACTERS,
                JacksonConfig.MAX_STRING_OR_NUMBER_LENGTH,
                JacksonConfig.MAX_NESTING_DEPTH);
    }

    /**
     * Spring's ProblemDetail has a {@code properties} map that is flattened into the body, never sent as a field;
     * {@code type} and {@code instance} are relative paths, so {@code uri-reference}, not {@code uri}.
     */
    @SuppressWarnings("unchecked") // swagger-models exposes properties as raw Schema
    private static void describeProblemDetailAsSent(OpenAPI openApi) {
        Map<String, Schema<?>> properties = (Map<String, Schema<?>>) (Map<?, ?>)
                openApi.getComponents().getSchemas().get("ProblemDetail").getProperties();
        properties.remove("properties");
        properties.get("type").setFormat("uri-reference");
        properties.get("instance").setFormat("uri-reference");
    }

    @SuppressWarnings("unchecked") // swagger-models exposes properties as raw Schema
    private static Schema<String> schemaProperty(OpenAPI openApi, String schema, String property) {
        return (Schema<String>)
                openApi.getComponents().getSchemas().get(schema).getProperties().get(property);
    }

    private static Map<String, Example> examples(io.swagger.v3.oas.models.media.Content content) {
        return content.get(PROBLEM_JSON).getExamples();
    }

    private static Example problem(String type, String title, int status, String detail) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "/problems/" + type);
        body.put("title", title);
        body.put("status", status);
        body.put("detail", detail);
        body.put("instance", PATH);
        return new Example().value(body);
    }
}
