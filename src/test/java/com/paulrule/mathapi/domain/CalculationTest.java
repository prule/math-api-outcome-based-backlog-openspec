package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalculationTest {

    private static String result(Operation operation, String a, String b) {
        return new Calculation(operation, new BigDecimal(a), new BigDecimal(b))
                .result()
                .toPlainString();
    }

    @ParameterizedTest
    @CsvSource({
        "0.1, 0.2, 0.3",
        "-1.5, -2.25, -3.75",
        "-1, 1, 0",
        "12345678901234567890.123456789, 0.000000001, 12345678901234567890.12345679",
        "1.10, 1.20, 2.3"
    })
    void addsExactly(String a, String b, String expected) {
        assertThat(result(Operation.ADD, a, b)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"1.50, 1.5, 0", "1.25, 3, -1.75", "0.3, 0.1, 0.2"})
    void subtractsExactly(String a, String b, String expected) {
        assertThat(result(Operation.SUBTRACT, a, b)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "123.45, 0, 0",
        "-2, 3, -6",
        "2, -3, -6",
        "-2, -3, 6",
        "1.5, 1.5, 2.25",
        "0.1, 0.1, 0.01",
        "1E+3, 1, 1000"
    })
    void multipliesExactly(String a, String b, String expected) {
        assertThat(result(Operation.MULTIPLY, a, b)).isEqualTo(expected);
    }

    @Test
    void resultHasTrailingZerosStrippedPerBr4() {
        var result = new Calculation(Operation.MULTIPLY, new BigDecimal("2.50"), new BigDecimal("2.0")).result();

        assertThat(result).isEqualByComparingTo("5").hasScaleOf(0);
    }

    @Test
    void zeroResultIsPlainZeroPerBr4() {
        var result = new Calculation(Operation.SUBTRACT, new BigDecimal("0.00"), new BigDecimal("0.000")).result();

        assertThat(result.toPlainString()).isEqualTo("0");
    }

    @Test
    void binaryOperationWithoutBIsRejectedPerBr9() {
        assertThatThrownBy(() -> new Calculation(Operation.ADD, BigDecimal.ONE, null))
                .isInstanceOf(ArityMismatchException.class);
    }
}
