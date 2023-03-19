package org.dessertj.samples;

import org.dessertj.assertions.SimpleCycleRenderer;
import org.dessertj.slicing.Classpath;
import org.junit.Test;

import static org.dessertj.assertions.SliceAssertions.dessert;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class SampleTest {
    private Classpath cp = new Classpath();

    @Test
    public void detectCycle() {
        try {
            dessert(cp.rootOf(Foo.class).getClazzes())
                    .renderCycleWith(new SimpleCycleRenderer())
                    .isCycleFree();
            fail("No AssertionError");
        } catch (AssertionError er) {
            String actual = er.getMessage().trim();
            assertThat(actual)
                    .startsWith("Cycle:")
                    .contains("clazz org.dessertj.samples.Bar")
                    .contains("clazz org.dessertj.samples.Baz")
                    .contains("clazz org.dessertj.samples.Foo");
            assertThat(actual.split("\n")).hasSize(5);
        }
    }

    @Test
    public void checkDependencies() {
        dessert(cp.rootOf(Foo.class).packageOf(Foo.class))
                .usesOnly(cp.slice("java.lang|io..*"));
    }

    @Test
    public void checkWithTestDependencies() {
        try {
            dessert(cp.packageOf(Foo.class))
                    .usesOnly(cp.slice("java.lang|io..*"));
            fail("No AssertionError");
        } catch (AssertionError er) {
            assertThat(er.getMessage().trim()).isEqualTo("Illegal Dependencies:\n" +
                    "org.dessertj.samples.SampleTest\n" +
                    " -> java.util.Set\n" +
                    " -> org.dessertj.assertions.CycleRenderer\n" +
                    " -> org.dessertj.assertions.SimpleCycleRenderer\n" +
                    " -> org.dessertj.assertions.SliceAssert\n" +
                    " -> org.dessertj.assertions.SliceAssertions\n" +
                    " -> org.dessertj.slicing.Classpath\n" +
                    " -> org.dessertj.slicing.Root\n" +
                    " -> org.dessertj.slicing.Slice\n" +
                    " -> org.fest.assertions.Assertions\n" +
                    " -> org.fest.assertions.Fail\n" +
                    " -> org.fest.assertions.ObjectArrayAssert\n" +
                    " -> org.fest.assertions.StringAssert\n" +
                    " -> org.junit.Test");
        }
    }

    @Test
    public void checkIllegalDependency() {
        try {
            dessert(cp.rootOf(Foo.class).packageOf(Foo.class))
                    .usesNot(cp.slice("java.io..*"));
            fail("No AssertionError");
        } catch (AssertionError er) {
            assertThat(er.getMessage().trim()).isEqualTo("Illegal Dependencies:\n" +
                    "org.dessertj.samples.Baz\n" +
                    " -> java.io.PrintStream");
        }
    }
}