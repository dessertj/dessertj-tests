package de.spricom.dessert.tests.classfile;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.samples.records.ComplexNumber;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class RecordsTest {

    @Test
    void test() throws IOException {
        System.out.println(new ClassFile(ComplexNumber.class).dump());
    }

    @Test
    void testComplexNumberDependencies() throws IOException {
        ClassFile cf = new ClassFile(ComplexNumber.class);
        assertThat(cf.getDependentClasses())
                .containsOnly("java.lang.Class",
                        "java.lang.Math",
                        "java.lang.Object",
                        "java.lang.Record",
                        "java.lang.String",
                        "java.lang.invoke.MethodHandle",
                        "java.lang.invoke.MethodHandles",
                        "java.lang.invoke.MethodHandles$Lookup",
                        "java.lang.invoke.TypeDescriptor",
                        "java.lang.runtime.ObjectMethods");
    }
}
