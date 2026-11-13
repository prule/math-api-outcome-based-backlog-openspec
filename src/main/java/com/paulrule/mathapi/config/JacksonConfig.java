package com.paulrule.mathapi.config;

import org.springframework.boot.jackson.autoconfigure.JsonFactoryBuilderCustomizer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

@Configuration
public class JacksonConfig {

    /** PRD §3.2 request limits, checked while tokenising, before any number is built. */
    static final int MAX_STRING_OR_NUMBER_LENGTH = 1000;

    static final int MAX_NESTING_DEPTH = 500;

    @Bean
    JsonFactoryBuilderCustomizer requestLimits() {
        return builder -> builder.streamReadConstraints(StreamReadConstraints.builder()
                .maxStringLength(MAX_STRING_OR_NUMBER_LENGTH)
                .maxNumberLength(MAX_STRING_OR_NUMBER_LENGTH)
                .maxNestingDepth(MAX_NESTING_DEPTH)
                .build());
    }

    /** A JSON number or boolean is not a string: {@code "operation": 123} is an invalid request, not a name. */
    @Bean
    JsonMapperBuilderCustomizer strictTextFields() {
        return builder -> builder.withCoercionConfig(
                LogicalType.Textual,
                config -> config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
    }
}
