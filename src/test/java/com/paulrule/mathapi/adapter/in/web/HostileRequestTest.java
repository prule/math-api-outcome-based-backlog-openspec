package com.paulrule.mathapi.adapter.in.web;

import static com.paulrule.mathapi.adapter.in.web.CalculationControllerTest.expectProblemDetails;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paulrule.mathapi.config.JacksonConfig;
import com.paulrule.mathapi.config.UseCaseConfig;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** BR-1 operand limits and PRD §3.2 request limits. */
@WebMvcTest(CalculationController.class)
@Import({UseCaseConfig.class, JacksonConfig.class})
class HostileRequestTest {

    private static final String HUNDRED_DIGITS = "9".repeat(100);
    private static final String VALID = "{\"operation\": \"ADD\", \"a\": 1, \"b\": 2}";

    @Autowired
    MockMvc mvc;

    private ResultActions calculate(String body) throws Exception {
        return mvc.perform(post("/api/v1/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(body));
    }

    private static String operands(String a, String b) {
        return "{\"operation\": \"ADD\", \"a\": \"%s\", \"b\": \"%s\"}".formatted(a, b);
    }

    /** The valid request, padded with spaces before its closing brace to exactly {@code length} characters. */
    private static String paddedTo(int length) {
        var body = VALID.substring(0, VALID.length() - 1) + " ".repeat(length - VALID.length()) + "}";
        assertThat(body).hasSize(length);
        return body;
    }

    private static String nested(int arrays) {
        return VALID.substring(0, VALID.length() - 1) + ", \"x\": " + "[".repeat(arrays) + "]".repeat(arrays) + "}";
    }

    private void expectInvalid(ResultActions result, String detailFragment) throws Exception {
        expectProblemDetails(result, 400);
        result.andExpect(jsonPath("$.type").value("/problems/invalid-request"))
                .andExpect(jsonPath("$.detail").value(containsString(detailFragment)));
    }

    static String[] atTheLimits() {
        return new String[] {"1E+1000", "1E-1000", HUNDRED_DIGITS};
    }

    static String[] pastTheLimits() {
        return new String[] {"1E+1001", "1E-1001", "1" + "0".repeat(100)};
    }

    @ParameterizedTest // BR-1
    @MethodSource("atTheLimits")
    void acceptsOperandsAtTheLimits(String operand) throws Exception {
        calculate(operands(operand, operand)).andExpect(status().isOk());
    }

    @ParameterizedTest // BR-1
    @MethodSource("pastTheLimits")
    void rejectsOperandOutOfRangeNamingItWithoutItsValue(String operand) throws Exception {
        var result = calculate(operands("1", operand));

        expectProblemDetails(result, 400);
        result.andExpect(jsonPath("$.type").value("/problems/operand-out-of-range"))
                .andExpect(jsonPath("$.detail").value(allOf(containsString("'b'"), not(containsString(operand)))));
    }

    @Test // BR-1: rejected from the written form, before any arithmetic
    void rejectsHugeExponentsImmediately() {
        assertTimeout(
                Duration.ofSeconds(1),
                () -> calculate("{\"operation\": \"MULTIPLY\", \"a\": \"1e999999999\", \"b\": \"1e999999999\"}")
                        .andExpect(jsonPath("$.type").value("/problems/operand-out-of-range")));
    }

    @Test // PRD §3.2 order: unknown operation and operand count (BR-9) before BR-1
    void unknownOperationAndOperandCountComeBeforeOperandLimits() throws Exception {
        calculate("{\"operation\": \"FOO\", \"a\": \"1E+1001\", \"b\": 1}")
                .andExpect(jsonPath("$.type").value("/problems/unknown-operation"));
        calculate("{\"operation\": \"ADD\", \"a\": \"1E+1001\"}")
                .andExpect(jsonPath("$.type").value("/problems/invalid-request"));
    }

    @Test
    void acceptsABodyOfExactlyTheLimit() throws Exception {
        calculate(paddedTo(16_384)).andExpect(status().isOk());
    }

    @Test
    void rejectsABodyOverTheLimitNamingIt() throws Exception {
        expectInvalid(calculate(paddedTo(16_385)), "16,384 characters");
    }

    @Test // PRD §3.2 counts characters: this body is within 16,384 characters but over 16,384 UTF-8 bytes
    void countsTheBodyInCharactersNotBytes() throws Exception {
        var field = "é".repeat(900);
        var extra = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            extra.append(", \"x").append(i).append("\": \"").append(field).append('"');
        }
        var body = VALID.substring(0, VALID.length() - 1) + extra + "}";
        assertThat(body.length()).isLessThanOrEqualTo(16_384);
        assertThat(body.getBytes(StandardCharsets.UTF_8).length).isGreaterThan(16_384);

        calculate(body).andExpect(status().isOk());
    }

    @ParameterizedTest // 15,000 digits: Jackson reports a number past its first text buffer as a string
    @ValueSource(ints = {1001, 15_000})
    void rejectsAHugeJsonNumberNamingTheLimit(int digits) throws Exception {
        expectInvalid(
                calculate("{\"operation\": \"ADD\", \"a\": " + "1".repeat(digits) + ", \"b\": 1}"),
                "string or number must be at most 1,000 characters");
    }

    @Test
    void rejectsAHugeJsonStringNamingTheLimit() throws Exception {
        expectInvalid(calculate(operands("1".repeat(1001), "1")), "string or number must be at most 1,000 characters");
    }

    @Test
    void acceptsNestingAtTheLimit() throws Exception {
        calculate(nested(499)).andExpect(status().isOk());
    }

    @Test
    void rejectsDeepNestingNamingTheLimit() throws Exception {
        expectInvalid(calculate(nested(500)), "500 levels");
    }
}
