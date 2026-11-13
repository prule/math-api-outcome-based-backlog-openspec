package com.paulrule.mathapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MathApiApplicationTest {

    @LocalServerPort
    int port;

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness"})
    void startedServiceReportsItselfHealthy(String path) throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(JsonPath.<String>read(response.body(), "$.status")).isEqualTo("UP");
        }
    }

    @Test
    void addsTwoOperandsEndToEnd() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/calculations"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(
                                    "{\"operation\": \"ADD\", \"a\": 0.1, \"b\": 0.2}"))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(JsonPath.<String>read(response.body(), "$.result")).isEqualTo("0.3");
        }
    }
}
