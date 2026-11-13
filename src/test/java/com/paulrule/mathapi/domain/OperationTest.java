package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class OperationTest {

    @ParameterizedTest
    @ValueSource(strings = {"ADD", "SUBTRACT", "MULTIPLY"})
    void resolvesExactNames(String name) {
        assertThat(Operation.named(name).name()).isEqualTo(name);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"add", "POWER", " ADD", ""})
    void rejectsAnyOtherName(String name) {
        assertThatThrownBy(() -> Operation.named(name)).isInstanceOf(UnknownOperationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADD", "SUBTRACT", "MULTIPLY"})
    void basicOperationsAreBinary(String name) {
        assertThat(Operation.named(name).arity()).isEqualTo(2);
    }
}
