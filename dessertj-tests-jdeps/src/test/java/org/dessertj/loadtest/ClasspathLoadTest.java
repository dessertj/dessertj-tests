package org.dessertj.loadtest;

import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Clazz;
import org.dessertj.slicing.Root;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dessertj.assertions.SliceAssertions.dessert;

public class ClasspathLoadTest {
    private static final Classpath cp = new Classpath();

    @Test
    void detectDuplicatesFilterByMavenArtifacts() {
        Slice duplicates = cp.duplicates().minus("module-info");
        var libs = duplicates.getClazzes().stream()
                .map(Clazz::getRoot)
                .map(Root::getURI)
                .sorted()
                .distinct()
                .toList();
        assertThat(libs).hasSize(0);
    }

    @Test
    void bigFailure() {
        try {
            dessert(cp).usesNot(cp.slice("java.lang..*"));
            throw new IllegalStateException("assertion didn't fail");
        } catch (AssertionError er) {
            System.out.println("Message length: " + er.getMessage().length());
            assertThat(er.getMessage()).hasSizeGreaterThan(5_000_000);
        }
    }
}
