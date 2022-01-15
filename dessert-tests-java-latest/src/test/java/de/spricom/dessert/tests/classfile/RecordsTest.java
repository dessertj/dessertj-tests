package de.spricom.dessert.tests.classfile;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.classfile.attribute.Attributes;
import de.spricom.dessert.classfile.attribute.RecordAttribute;
import de.spricom.dessert.samples.records.ComplexNumber;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RecordsTest {

    @Disabled
    @Test
    void dump() throws IOException {
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

    @Test
    void testRecordAttribute() throws IOException {
        ClassFile cf = new ClassFile(ComplexNumber.class);
        RecordAttribute record = record(cf);

        assertThat(record.getComponents()).hasSize(2);
        for (RecordAttribute.RecordComponentInfo component : record.getComponents()) {
            assertThat(component.getAttributes()).isEmpty();
        }

        Set<String> dependentClasses = new HashSet<>();
        record.addDependentClassNames(dependentClasses);
        assertThat(dependentClasses).isEmpty();
    }

    private RecordAttribute record(ClassFile cf) {
        List<RecordAttribute> attributes = Attributes.filter(cf.getAttributes(), RecordAttribute.class);
        assertThat(attributes).hasSize(1);
        return attributes.get(0);
    }
}
