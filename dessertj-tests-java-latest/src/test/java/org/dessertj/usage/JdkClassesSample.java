package org.dessertj.usage;

import org.dessertj.classfile.ClassFile;
import org.dessertj.slicing.Classpath;
import org.dessertj.util.ClassUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Shows the behavior of some dessert classes regarding path resolution.
 * Almost the same sample can be found in the JDK-6 Samples project,
 * hence one can see the differences to the latest JDK here.
 */
public class JdkClassesSample {

    /**
     * Shows the result of getting an URI for a class or a path.
     */
    @Test
    public void showUri() {
        System.out.println(ClassUtils.getURI(List.class));
        URL resource = List.class.getResource("List.class");
        System.out.println(resource);
        System.out.println(List.class.getResource("/java/util"));
        System.out.println(List.class.getResource("/java/util/"));
        System.out.println(List.class.getResource("/"));
    }

    /**
     * Tries to read content of a package directory.
     * Shows this is not possible by simply using resource URLs.
     */
    @Test
    public void packageContent() throws IOException {
        URL resource = List.class.getResource("List.class");
        System.out.println(resource.getContent());
        System.out.println(resource.getPath());
        URL parent = new URL(resource, "/java/util");
        System.out.println(parent);
        try {
            System.out.println(parent.getContent());
            Assertions.fail("no exception");
        } catch (IOException ex) {
            System.out.println("expected: " + ex);
        }
    }

    /**
     * Shows the dependencies of a single class.
     */
    @Test
    public void showDependencies() throws IOException {
        ClassFile cf = new ClassFile(List.class);
        for (String dependentClass : cf.getDependentClasses()) {
            System.out.println(dependentClass);
        }
    }

    /**
     * Shows the root URI of for a JDK class (for Java 6, 7 and 8)
     */
    @Test
    public void root() {
        Classpath cp = new Classpath();
        assertThatCode(() -> cp.rootOf(List.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("java.util.List not found within this classpath.");
    }
}
