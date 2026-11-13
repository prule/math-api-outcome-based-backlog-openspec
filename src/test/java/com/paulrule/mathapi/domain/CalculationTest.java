package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest // BR-3: 34 significant digits, HALF_EVEN
    @CsvSource({
        "1, 3, 0.3333333333333333333333333333333333",
        "2, 3, 0.6666666666666666666666666666666667",
        "1, 7, 0.1428571428571428571428571428571429",
        "1, -3, -0.3333333333333333333333333333333333",
        "-7, 2, -3.5"
    })
    void dividesToThirtyFourSignificantDigits(String a, String b, String expected) {
        assertThat(result(Operation.DIVIDE, a, b)).isEqualTo(expected);
    }

    @ParameterizedTest // BR-3: a tie at the 35th digit rounds to the even 34th digit
    @CsvSource({
        "10000000000000000000000000000000005, 10000000000000000000000000000000000",
        "10000000000000000000000000000000015, 10000000000000000000000000000000020",
        "10000000000000000000000000000000025, 10000000000000000000000000000000020"
    })
    void roundsTiesToEven(String a, String expected) {
        assertThat(result(Operation.DIVIDE, a, "1")).isEqualTo(expected);
    }

    @ParameterizedTest // BR-4: an exact quotient is returned exactly, trailing zeros stripped
    @CsvSource({"1, 4, 0.25", "10, 4, 2.5", "6.0, 2, 3"})
    void returnsExactQuotientsExactly(String a, String b, String expected) {
        assertThat(result(Operation.DIVIDE, a, b)).isEqualTo(expected);
    }

    @ParameterizedTest // BR-2: any zero representation
    @ValueSource(strings = {"0", "0.00", "-0", "0E+5"})
    void rejectsDivisionByZero(String zero) {
        var calculation = new Calculation(Operation.DIVIDE, BigDecimal.ONE, new BigDecimal(zero));

        assertThatThrownBy(calculation::result).isInstanceOf(DivisionByZeroException.class);
    }
}
