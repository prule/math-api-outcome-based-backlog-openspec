package com.paulrule.mathapi.config;

import com.paulrule.mathapi.domain.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documented values that come from the domain are generated here, never written in annotations (STANDARDS §4). */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenApiCustomizer domainValues() {
        List<String> operations =
                Arrays.stream(Operation.values()).map(Operation::name).toList();
        return openApi -> {
            openApi.info(new Info()
                    .title("Math API")
                    .version("v1")
                    .description("Exact decimal arithmetic. Every error is RFC 9457 Problem Details."));

            @SuppressWarnings("unchecked") // swagger-models exposes properties as raw Schema
            Schema<String> operation = (Schema<String>) openApi.getComponents()
                    .getSchemas()
                    .get("CalculationRequest")
                    .getProperties()
                    .get("operation");
            operation.setEnum(operations);

            Map<String, Object> unknown = new LinkedHashMap<>();
            unknown.put("type", "/problems/unknown-operation");
            unknown.put("title", "Unknown operation");
            unknown.put("status", 400);
            unknown.put(
                    "detail",
                    "Unknown operation. Supported operations: "
                            + operations.stream().collect(Collectors.joining(", ")) + ".");
            unknown.put("instance", "/api/v1/calculations");
            openApi.getPaths()
                    .get("/api/v1/calculations")
                    .getPost()
                    .getResponses()
                    .get("400")
                    .getContent()
                    .get("application/problem+json")
                    .getExamples()
                    .put("unknown-operation", new Example().value(unknown));
        };
    }
}
