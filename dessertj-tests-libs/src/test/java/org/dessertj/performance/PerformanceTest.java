package org.dessertj.performance;

import org.dessertj.modules.ModuleRegistry;
import org.dessertj.modules.fixed.JavaModules;
import org.dessertj.samples.DetectingUsageOfInternalClassesTest;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.dessertj.assertions.SliceAssertions.dessert;
import static org.junit.jupiter.api.Assertions.assertTimeout;

public class PerformanceTest {
    private static final Classpath cp = new Classpath();
    private static final ModuleRegistry mr = new ModuleRegistry(cp);
    private static final JavaModules java = new JavaModules(mr);
    private static final Slice spring = cp.slice("org.springframework..*");
    private static final Slice dessertj = cp.slice("org.dessertj..*");
    private static final Slice junit = cp.slice("org.junit|hamcrest..*");
    private static final Slice assertj = cp.slice("org.assertj..*");
    private static final Slice log4j = cp.slice("..log4j..*");

    @Test
    void testUsesOnly() {
        assertTimeout(Duration.of(500, ChronoUnit.MILLIS),
                () -> dessert(cp.rootOf(this.getClass()).minus(cp.asClazz(DetectingUsageOfInternalClassesTest.class)))
                        .usesOnly(java.base, spring, dessertj, junit, assertj, log4j));
    }
}
