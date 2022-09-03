package de.spricom.dessert.performance;

import de.spricom.dessert.modules.ModuleRegistry;
import de.spricom.dessert.modules.fixed.JavaModules;
import de.spricom.dessert.samples.DetectingUsageOfInternalClassesTest;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;
import static org.junit.jupiter.api.Assertions.assertTimeout;

public class PerformanceTest {
    private static final Classpath cp = new Classpath();
    private static final ModuleRegistry mr = new ModuleRegistry(cp);
    private static final JavaModules java = new JavaModules(mr);
    private static final Slice spring = cp.slice("org.springframework..*");
    private static final Slice dessert = cp.slice("de.spricom..*");
    private static final Slice junit = cp.slice("org.junit|hamcrest..*");
    private static final Slice assertj = cp.slice("org.assertj..*");
    private static final Slice log4j = cp.slice("..log4j..*");

    @Test
    void testUsesOnly() {
        assertTimeout(Duration.of(500, ChronoUnit.MILLIS),
                () -> dessert(cp.rootOf(this.getClass()).minus(cp.asClazz(DetectingUsageOfInternalClassesTest.class)))
                        .usesOnly(java.base, spring, dessert, junit, assertj, log4j));
    }
}
