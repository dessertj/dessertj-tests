package org.dessertj.samples.patterns;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class SwitchSamplesTest {

    @Test
    void testByNumber() {
        assertThat(SwitchSamples.byNumber(0)).isEqualTo("zero");
        assertThat(SwitchSamples.byNumber(1)).isEqualTo("one");
        assertThat(SwitchSamples.byNumber(42)).isEqualTo("any");
    }

    @Test
    void testAsDouble() {
        assertThat(SwitchSamples.asDouble(BigDecimal.valueOf(5555, 2))).isEqualTo(55.55);
        assertThat(SwitchSamples.asDouble("2.34")).isEqualTo(2.34);
        assertThat(SwitchSamples.asDouble(null)).isZero();
        assertThat(SwitchSamples.asDouble(new Random())).isNaN();
        assertThat(SwitchSamples.asDouble("")).isNaN();
    }
}