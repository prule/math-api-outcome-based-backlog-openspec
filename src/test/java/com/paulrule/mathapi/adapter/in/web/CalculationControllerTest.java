package com.paulrule.mathapi.adapter.in.web;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paulrule.mathapi.config.UseCaseConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CalculationController.class)
@Import(UseCaseConfig.class)
class CalculationControllerTest {

    private static final String PROBLEM = "/problems/";

    @Autowired
    MockMvc mvc;

    private ResultActions calculate(String body) throws Exception {
        return mvc.perform(post("/api/v1/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void expectProblem(ResultActions result, int status, String type) throws Exception {
        result.andExpect(status().is(status))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value(PROBLEM + type))
                .andExpect(jsonPath("$.title").isString())
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.detail")
                        .value(allOf(not(containsString("Exception")), not(containsString("java.")))));
    }

    @Test
    void addsDecimalsExactly() throws Exception {
        calculate("""
                        {"operation": "ADD", "a": 0.1, "b": 0.2}""")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.operation").value("ADD"))
                .andExpect(jsonPath("$.a").value("0.1"))
                .andExpect(jsonPath("$.b").value("0.2"))
                .andExpect(jsonPath("$.result").value("0.3"));
    }

    @ParameterizedTest
    @CsvSource({"SUBTRACT, 1.25, 3, -1.75", "MULTIPLY, 1.5, 1.5, 2.25", "SUBTRACT, 1.50, 1.5, 0"})
    void subtractsAndMultipliesExactly(String operation, String a, String b, String result) throws Exception {
        calculate("""
                        {"operation": "%s", "a": %s, "b": %s}""".formatted(operation, a, b))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(result));
    }

    @Test
    void acceptsStringAndNumberOperandsAndReturnsStrings() throws Exception {
        calculate("""
                        {"operation": "ADD", "a": "0.1", "b": 0.2}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value("0.1"))
                .andExpect(jsonPath("$.b").value("0.2"))
                .andExpect(jsonPath("$.result").value("0.3"));
    }

    @Test
    void echoesOperandsWithTheirScaleInPlainNotation() throws Exception {
        calculate("""
                        {"operation": "ADD", "a": "1.50", "b": 1e2}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value("1.50"))
                .andExpect(jsonPath("$.b").value("100"))
                .andExpect(jsonPath("$.result").value("101.5"));
    }

    @Test
    void returnsResultInPlainNotation() throws Exception {
        calculate("""
                        {"operation": "MULTIPLY", "a": "1E+3", "b": "1"}""").andExpect(status().isOk()).andExpect(jsonPath("$.result").value("1000"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{not json",
                "{\"a\": 1, \"b\": 2}",
                "{\"operation\": \"ADD\", \"b\": 2}",
                "{\"operation\": \"ADD\", \"a\": \"abc\", \"b\": 2}",
                "{\"operation\": \"ADD\", \"a\": true, \"b\": 2}",
                "{\"operation\": \"ADD\", \"a\": \"\", \"b\": 2}",
                "{\"operation\": \"ADD\", \"a\": 1}",
                "{\"operation\": \"ADD\", \"a\": 1, \"b\": null}"
            })
    void rejectsMalformedRequestsAsInvalid(String body) throws Exception {
        expectProblem(calculate(body), 400, "invalid-request");
    }

    @ParameterizedTest
    @ValueSource(strings = {"POWER", "add"})
    void rejectsUnknownOperationsListingTheSupportedOnes(String operation) throws Exception {
        var result = calculate("""
                {"operation": "%s", "a": 1, "b": 2}""".formatted(operation));

        expectProblem(result, 400, "unknown-operation");
        result.andExpect(jsonPath("$.detail")
                .value(allOf(containsString("ADD"), containsString("SUBTRACT"), containsString("MULTIPLY"))));
    }

    @Test
    void reportsUnknownOperationBeforeMissingB() throws Exception {
        expectProblem(calculate("""
                {"operation": "FOO", "a": 1}"""), 400, "unknown-operation");
    }

    @Test
    void unsupportedMediaTypeIsProblemDetails() throws Exception {
        mvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("ADD 1 2"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").isString())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.detail").isString());
    }

    @Test
    void unsupportedMethodIsProblemDetails() throws Exception {
        mvc.perform(get("/api/v1/calculations"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(405));
    }
}
