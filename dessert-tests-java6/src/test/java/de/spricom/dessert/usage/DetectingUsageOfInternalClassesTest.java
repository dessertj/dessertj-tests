package de.spricom.dessert.usage;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.Base64Data;
import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import org.junit.Test;

import java.io.IOException;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Shows how the usage of an internal class can be detected.
 */
// TODO: Resolve compilation error when compiling this by maven.
public class DetectingUsageOfInternalClassesTest {

    /**
     * Uses an internal class
     */
    @Test
    public void showDependencies() throws IOException {
        ClassFile cf = new ClassFile(Base64Data.class);
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
            assertThat(er.getMessage().trim()).isEqualTo("Illegal Dependencies:\n" +
                    "de.spricom.dessert.usage.DetectingUsageOfInternalClassesTest\n" +
                    " -> com.sun.xml.internal.bind.v2.runtime.unmarshaller.Base64Data");
        }
    }
}
