package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DivideTest {

    private static String divide(String a, String b) {
        return new Calculation(Operation.DIVIDE, new BigDecimal(a), new BigDecimal(b))
                .result()
                .toPlainString();
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
        assertThat(divide(a, b)).isEqualTo(expected);
    }

    @ParameterizedTest // BR-3: a tie at the 35th digit rounds to the even 34th digit
    @CsvSource({
        "10000000000000000000000000000000005, 10000000000000000000000000000000000",
        "10000000000000000000000000000000015, 10000000000000000000000000000000020",
        "10000000000000000000000000000000025, 10000000000000000000000000000000020"
    })
    void roundsTiesToEven(String a, String expected) {
        assertThat(divide(a, "1")).isEqualTo(expected);
    }

    @ParameterizedTest // BR-4: an exact quotient is returned exactly, trailing zeros stripped
    @CsvSource({"1, 4, 0.25", "10, 4, 2.5", "6.0, 2, 3"})
    void returnsExactQuotientsExactly(String a, String b, String expected) {
        assertThat(divide(a, b)).isEqualTo(expected);
    }

    @ParameterizedTest // BR-2: any zero representation
    @ValueSource(strings = {"0", "0.00", "-0", "0E+5"})
    void rejectsDivisionByZero(String zero) {
        var calculation = new Calculation(Operation.DIVIDE, BigDecimal.ONE, new BigDecimal(zero));

        assertThatThrownBy(calculation::result).isInstanceOf(DivisionByZeroException.class);
    }
}
