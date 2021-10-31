package de.spricom.dessert.usage;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Root;
import de.spricom.dessert.util.ClassUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Shows the behavior of some dessert classes regarding path resolution.
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
            Assert.fail("no exception");
        } catch (FileNotFoundException ex) {
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
        Root root = cp.rootOf(List.class);
        System.out.println(root.getURI());
    }
}
