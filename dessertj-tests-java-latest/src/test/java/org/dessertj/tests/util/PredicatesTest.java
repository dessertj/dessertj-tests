package org.dessertj.tests.util;

import org.dessertj.util.Predicate;
import org.dessertj.util.Predicates;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PredicatesTest {

    @Test
    void testAnd() {
        assertThat(
                Predicates.and(
                        Predicates.any(),
                        Predicates.not(Predicates.none())
                ).test(null)
        ).isTrue();
        assertThat(
                Predicates.and(
                        Predicates.any(),
                        Predicates.not(Predicates.none()),
                        Predicates.none()
                ).test(null)
        ).isFalse();
    }

    @Test
    void testOr() {
        assertThat(
                Predicates.or(
                        Predicates.none(),
                        Predicates.not(Predicates.any())
                ).test(null)
        ).isFalse();
        assertThat(
                Predicates.or(
                        Predicates.none(),
                        Predicates.not(Predicates.any()),
                        Predicates.any()
                ).test(null)
        ).isTrue();
    }

    @Test
    void testGeneric() {
        assertThat(checkSomething(List.of(new Something(1), new Something(2)),
                this::isPositive)).isTrue();
        assertThat(checkSomething(List.of(new Something(1), new Something(0)),
                this::isPositive)).isFalse();
        assertThat(checkSomething(List.of(new Something(1), new Something(2)),
                Predicates.any())).isTrue();
    }

    private boolean isPositive(Something something) {
        return something.value > 0;
    }

    private boolean checkSomething(Collection<Something> somethings, Predicate<Something> check) {
        for (Something something : somethings) {
            if (!check.test(something)) {
                return false;
            }
        }
        return true;
    }

    static class Something {
        final int value;

        Something(int value) {
            this.value = value;
        }
    }
}
