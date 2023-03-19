package org.dessertj.usage;

import com.sun.net.httpserver.HttpServer;
import org.dessertj.classfile.ClassFile;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Clazz;
import org.junit.Test;

import java.io.IOException;

import static org.dessertj.assertions.SliceAssertions.dessert;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Shows how the usage of an internal class can be detected.
 */
public class DetectingUsageOfInternalClassesTest {

    /**
     * Uses an internal class
     */
    @Test
    public void showDependencies() throws IOException {
        ClassFile cf = new ClassFile(HttpServer.class);
        for (String dependentClass : cf.getDependentClasses()) {
            System.out.println(dependentClass);
        }
    }

    /**
     * Make sure usage of an internal class is detected.
     */
    @Test
    public void testDoesNotUseInternalClasses() {
        Classpath cp = new Classpath();
        Clazz me = cp.asClazz(this.getClass());

        try {
            dessert(me).usesNot(cp.slice("..sun..*"));
            throw new IllegalStateException("Usage of internal class not detected."); // Cannot throw AssertionError here
        } catch (AssertionError er) {
            System.out.println(er.getMessage());
            assertThat(er.getMessage())
                    .contains("Illegal Dependencies:")
                    .contains("org.dessertj.usage.DetectingUsageOfInternalClassesTest")
                    .contains(" -> com.sun.")
                    .contains(".HttpServer");
        }
    }
}
