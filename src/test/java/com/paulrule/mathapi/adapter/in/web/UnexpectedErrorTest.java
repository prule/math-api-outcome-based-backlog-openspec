package com.paulrule.mathapi.adapter.in.web;

import static com.paulrule.mathapi.adapter.in.web.CalculationControllerTest.expectProblemDetails;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.paulrule.mathapi.application.PerformCalculation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CalculationController.class)
class UnexpectedErrorTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PerformCalculation performCalculation;

    @Test
    void unexpectedFailureIsProblemDetailsThatRevealsNothing() throws Exception {
        given(performCalculation.perform(any(), any(), any()))
                .willThrow(new IllegalStateException("secret internal state"));

        var result = mvc.perform(post("/api/v1/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operation\": \"ADD\", \"a\": 1, \"b\": 2}"));

        expectProblemDetails(result, 500);
        result.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail")
                .value("The server could not complete the request."));
    }
}
