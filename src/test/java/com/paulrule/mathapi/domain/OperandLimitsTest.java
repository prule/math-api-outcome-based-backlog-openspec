package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** BR-1: at most 100 significant digits and a scale within ±1000, as written. */
class OperandLimitsTest {

    static final String HUNDRED_DIGITS = "9".repeat(100);
    static final String HUNDRED_AND_ONE_DIGITS = "1" + "0".repeat(100);

    static String[] atTheLimits() {
        return new String[] {HUNDRED_DIGITS, "1E+1000", "1E-1000", "-" + HUNDRED_DIGITS};
    }

    static String[] pastTheLimits() {
        // 1E+2147483648 has scale Integer.MIN_VALUE, whose Math.abs is still negative
        return new String[] {HUNDRED_AND_ONE_DIGITS, "1E+1001", "1E-1001", "1e999999999", "1E+2147483648"};
    }

    @ParameterizedTest
    @MethodSource("atTheLimits")
    void acceptsOperandsExactlyAtTheLimits(String operand) {
        var value = new BigDecimal(operand);

        assertThatNoException().isThrownBy(() -> new Calculation(Operation.ADD, value, value));
    }

    @ParameterizedTest
    @MethodSource("pastTheLimits")
    void rejectsAOutsideTheLimitsNamingItWithoutItsValue(String operand) {
        assertThatThrownBy(() -> new Calculation(Operation.ADD, new BigDecimal(operand), BigDecimal.ONE))
                .isInstanceOf(OperandOutOfRangeException.class)
                .hasMessageContaining("'a'")
                .hasMessageNotContaining(operand);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1E+1001", "1E-1001"})
    void rejectsBOutsideTheLimitsNamingIt(String operand) {
        assertThatThrownBy(() -> new Calculation(Operation.MULTIPLY, BigDecimal.ONE, new BigDecimal(operand)))
                .isInstanceOf(OperandOutOfRangeException.class)
                .hasMessageContaining("'b'");
    }

    @Test // DOMAIN-MODEL §2: BR-9 is checked before BR-1
    void arityIsCheckedBeforeOperandLimits() {
        assertThatThrownBy(() -> new Calculation(Operation.ADD, new BigDecimal("1E+1001"), null))
                .isInstanceOf(ArityMismatchException.class);
    }

    @Test // BR-1 is checked on the written form: 1E+100 has one digit even though its value has 101
    void checksTheOperandAsWritten() {
        assertThat(new BigDecimal("1E+100").precision()).isEqualTo(1);
        assertThatNoException()
                .isThrownBy(() -> new Calculation(Operation.ADD, new BigDecimal("1E+100"), BigDecimal.ONE));
    }
}
