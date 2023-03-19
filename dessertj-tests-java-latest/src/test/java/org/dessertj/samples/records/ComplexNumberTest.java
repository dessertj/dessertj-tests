package org.dessertj.samples.records;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexNumberTest {
    public static final Offset<Double> DOUBLE_OFFSET = Offset.offset(1e-15);

    private final ComplexNumber zero = new ComplexNumber(0);
    private final ComplexNumber one = new ComplexNumber(1);
    private final ComplexNumber i = new ComplexNumber(0, 1);

    @Test
    void testAdd() {
        assertThat(one.plus(one)).isEqualTo(new ComplexNumber(2));
        assertThat(one.plus(i)).isEqualTo(new ComplexNumber(1, 1));
    }

    @Test
    void testSubtract() {
        assertThat(new ComplexNumber(1, 1).minus(i).minus(one)).isEqualTo(zero);
    }

    @Test
    void testMulitply() {
        assertThat(i.times(i)).isEqualTo(new ComplexNumber(-1));
    }

    @Test
    void testDivide() {
        assertThat(one.div(i)).isEqualTo(new ComplexNumber(0, -1));
    }

    @Test
    void testExponantion_i2() {
        ComplexNumber actual = i.power(new ComplexNumber(2));
        ComplexNumber expected = new ComplexNumber(-1);
        assertThat(actual.a()).isCloseTo(expected.a(), DOUBLE_OFFSET);
        assertThat(actual.bi()).isCloseTo(expected.bi(), DOUBLE_OFFSET);
    }

    @Test
    void testExponantion_i() {
        assertThat(i.power(i)).isEqualTo(new ComplexNumber(Math.exp(-Math.PI * 0.5)));
    }

    @Test
    void testExponantion_1i() {
        var x = new ComplexNumber(1, 1);
        var phi = Math.PI / 4.0 + Math.log(2) / 2.0;
        var r = Math.sqrt(2) * Math.exp(Math.PI * -0.25);
        assertThat(x.power(x)).isEqualTo(new ComplexNumber(r * Math.cos(phi), r * Math.sin(phi)));
    }

    @Test
    void testExponantion() {
        ComplexNumber actual = new ComplexNumber(-4).power(new ComplexNumber(0.5));
        ComplexNumber expected = new ComplexNumber(0, 2);
        assertThat(actual.a()).isCloseTo(expected.a(), DOUBLE_OFFSET);
        assertThat(actual.bi()).isCloseTo(expected.bi(), DOUBLE_OFFSET);
    }
}