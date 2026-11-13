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

    private static List<String> strings(JsonNode array) {
        return array.valueStream().map(JsonNode::asString).toList();
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
        assertThat(strings(schema("CalculationRequest").at("/properties/operation/enum")))
                .containsExactlyElementsOf(
                        Arrays.stream(Operation.values()).map(Operation::name).toList());
    }

    /** Not a tautology: the documented names are sent to the API, which must accept each one. */
    @Test
    void everyDocumentedOperationIsAccepted() throws Exception {
        for (var operation : strings(schema("CalculationRequest").at("/properties/operation/enum"))) {
            mvc.perform(post("/api/v1/calculations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"operation\": \"" + operation + "\", \"a\": 6, \"b\": 3}"))
                    .andExpect(status().isOk());
        }
    }

    /** "From the docs alone": the rules are stated with the numbers the code enforces, not BR or PRD references. */
    @Test
    void statesTheRulesAClientNeeds() {
        assertThat(calculate.get("description").asString())
                .contains(
                        "34 significant digits",
                        "HALF_EVEN",
                        "at most 100 significant digits",
                        "scale between -1000 and 1000",
                        "at most 16,384 characters",
                        "at most 1,000 characters",
                        "at most 500 levels",
                        "invalid request, unknown operation, operand count, operand out of range, division by zero")
                .doesNotContain("BR-", "PRD");
    }

    @Test
    void documentsOperandsAsNumberOrString() {
        for (var operand : List.of("a", "b")) {
            assertThat(strings(schema("CalculationRequest").at("/properties/" + operand + "/type")))
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

        var response = schema("CalculationResponse");
        assertThat(names(response.get("properties"))).containsExactlyInAnyOrder("operation", "a", "b", "result");
        response.get("properties")
                .forEach(property -> assertThat(property.get("type").asString()).isEqualTo("string"));
        assertThat(strings(response.get("required"))).containsExactlyInAnyOrder("operation", "a", "b", "result");
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

    @Test
    void describesProblemDetailsAsTheApiSendsThem() {
        var properties = schema("ProblemDetail").get("properties");
        assertThat(names(properties)).doesNotContain("properties");
        assertThat(properties.at("/type/format").asString()).isEqualTo("uri-reference");
        assertThat(properties.at("/instance/format").asString()).isEqualTo("uri-reference");
    }

    /** Each named request example, sent to the API, returns exactly the response example of the same name. */
    @Test
    void everyExampleIsWhatTheApiReallySends() throws Exception {
        Map<String, Expected> expected = new HashMap<>();
        calculate.get("responses").properties().forEach(response -> {
            int status = Integer.parseInt(response.getKey());
            response.getValue()
                    .get("content")
                    .forEach(media -> media.get("examples")
                            .properties()
                            .forEach(example -> expected.put(
                                    example.getKey(),
                                    new Expected(status, example.getValue().get("value")))));
        });
        var requests = calculate.at("/requestBody/content/application~1json/examples");

        assertThat(names(requests)).containsExactlyInAnyOrderElementsOf(expected.keySet());
        for (var name : names(requests)) {
            var sent = mvc.perform(post("/api/v1/calculations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requests.at("/" + name + "/value").toString()))
                    .andReturn()
                    .getResponse();

            assertThat(sent.getStatus()).as(name).isEqualTo(expected.get(name).status());
            assertThat(JSON.readTree(sent.getContentAsString()))
                    .as(name)
                    .isEqualTo(expected.get(name).body());
        }
    }

    private record Expected(int status, JsonNode body) {}
}
