package org.dessertj.samples.api;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class StreamTest {

    @Test
    void testToList() {
        List<Integer> integerList = IntStream.range(1, 10).boxed().toList();
        assertThat(integerList).hasSize(9);
    }

    @Test
    void testMapMulti() {
        int[] result = IntStream.range(1, 10).boxed()
                .mapMultiToInt((i, m) -> IntStream.range(0, i).forEach(m::accept))
                .toArray();
        System.out.println(Arrays.toString(result));
        assertThat(result).hasSize(45);
    }
}
