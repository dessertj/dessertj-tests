package org.dessertj.tutorial;

import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.dessertj.assertions.SliceAssertions.dessert;

public class InternalApisTest {
    private static final Classpath cp = new Classpath();

    @Disabled("will fail")
    @Test
    @DisplayName("Detect usage of internal APIs")
    void detectSpringInternalApis() {
        Slice springframework = cp.slice("org.springframework..*");
        dessert(springframework).usesNot(
                cp.slice("com.sun..*"),
                cp.slice("sun..*")
        );
    }

    @Test
    @DisplayName("Make sure springframework adds no internal API usages")
    void detectSpringAddionalInternalApis() {
        Slice springframework = cp.slice("org.springframework..*")
                .minus(cp.slice("org.springframework.objenesis.instantiator.sun|util.*"))
                .minus(cp.slice("org.springframework.remoting..*"));
        dessert(springframework).usesNot(
                cp.slice("com.sun..*"),
                cp.slice("sun..*")
        );
    }
}
