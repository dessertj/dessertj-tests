package de.spricom.dessert.samples.patterns;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PatternMatchingTest {

    @Test
    void test() {
        assertThat(size("abc")).isEqualTo(3);
        assertThat(size(1)).isEqualTo(1);
        assertThat(size(null)).isEqualTo(1);
    }

    int size(Object o) {
        if (o instanceof String s) {
            return s.length();
        }
        return 1;
    }

    @Test
    void test2() {
        assertThat(size2("abc")).isEqualTo(3);
        assertThat(size2(1)).isEqualTo(1);
        assertThat(size2(null)).isEqualTo(1);
    }

    int size2(Object o) {
        if (!(o instanceof String s)) {
            return 1;
        }
        return s.length();
    }
}
