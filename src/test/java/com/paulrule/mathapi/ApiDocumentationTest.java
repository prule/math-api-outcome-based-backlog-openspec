package com.paulrule.mathapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paulrule.mathapi.domain.Operation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** STANDARDS.md §4: the generated API docs describe the real API. springdoc is not part of the web slice. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiDocumentationTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String PROBLEM_JSON = "application/problem+json";

    @Autowired
    MockMvc mvc;

    JsonNode docs;
    JsonNode calculate;

    @BeforeEach
    void readDocs() throws Exception {
        var body = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        docs = JSON.readTree(body);
        calculate = docs.at("/paths/~1api~1v1~1calculations/post");
    }

    private JsonNode schema(String name) {
        return docs.at("/components/schemas/" + name);
    }

    private static List<String> names(JsonNode node) {
        return node.propertyNames().stream().toList();
    }

    @Test
    void servesAnOpenApi31DocumentListingTheCalculationsEndpoint() {
        assertThat(docs.get("openapi").asString()).startsWith("3.1");
        assertThat(calculate.isMissingNode()).isFalse();
    }

    @Test
    void servesTheInteractiveUi() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void documentsExactlyTheOperationsTheDomainSupports() {
        var documented = StreamSupport.stream(
                        schema("CalculationRequest")
                                .at("/properties/operation/enum")
                                .spliterator(),
                        false)
                .map(JsonNode::asString)
                .toList();

        assertThat(documented)
                .containsExactlyElementsOf(
                        Arrays.stream(Operation.values()).map(Operation::name).toList());
    }

    @Test
    void documentsOperandsAsNumberOrString() {
        for (var operand : List.of("a", "b")) {
            var types = schema("CalculationRequest").at("/properties/" + operand + "/type");
            assertThat(types.valueStream().map(JsonNode::asString).toList())
                    .as(operand)
                    .containsExactlyInAnyOrder("number", "string");
        }
        assertThat(names(calculate.at("/requestBody/content/application~1json/examples")))
                .contains("numbers", "strings");
    }

    @Test
    void documentsTheResultAsAllStrings() {
        var ok = calculate.at("/responses/200/content");
        assertThat(names(ok)).containsExactly("application/json");

        var properties = schema("CalculationResponse").get("properties");
        assertThat(names(properties)).containsExactlyInAnyOrder("operation", "a", "b", "result");
        properties.forEach(
                property -> assertThat(property.get("type").asString()).isEqualTo("string"));
        assertThat(names(ok.at("/application~1json/examples"))).isNotEmpty();
    }

    @Test
    void documentsEveryProblemTypeWithAnExample() {
        var expected = Map.of(
                "400", Set.of("invalid-request", "unknown-operation", "operand-out-of-range"),
                "422", Set.of("division-by-zero"));

        expected.forEach((status, types) -> {
            var content = calculate.at("/responses/" + status + "/content");
            assertThat(names(content)).as(status).containsExactly(PROBLEM_JSON);
            var examples = content.get(PROBLEM_JSON).get("examples");
            assertThat(names(examples)).as(status).containsExactlyInAnyOrderElementsOf(types);
            types.forEach(
                    type -> assertThat(examples.at("/" + type + "/value/type").asString())
                            .isEqualTo("/problems/" + type));
        });
    }

    /** Each named request example, sent to the API, returns exactly the response example of the same name. */
    @Test
    void everyExampleIsWhatTheApiReallySends() throws Exception {
        var requests = calculate.at("/requestBody/content/application~1json/examples");
        Map<String, Integer> statusOf = new HashMap<>();
        Map<String, JsonNode> responseOf = new HashMap<>();
        calculate
                .get("responses")
                .properties()
                .forEach(response -> response.getValue()
                        .get("content")
                        .properties()
                        .forEach(media -> media.getValue()
                                .get("examples")
                                .properties()
                                .forEach(example -> {
                                    statusOf.put(example.getKey(), Integer.valueOf(response.getKey()));
                                    responseOf.put(
                                            example.getKey(), example.getValue().get("value"));
                                })));

        assertThat(names(requests)).containsExactlyInAnyOrderElementsOf(responseOf.keySet());
        for (var name : names(requests)) {
            var sent = mvc.perform(post("/api/v1/calculations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requests.at("/" + name + "/value").toString()))
                    .andReturn()
                    .getResponse();

            assertThat(sent.getStatus()).as(name).isEqualTo(statusOf.get(name));
            assertThat(JSON.readTree(sent.getContentAsString())).as(name).isEqualTo(responseOf.get(name));
        }
    }
}
