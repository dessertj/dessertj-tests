package de.spricom.dessert.tests.classfile;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.classfile.attribute.AttributeInfo;
import de.spricom.dessert.classfile.attribute.Attributes;
import de.spricom.dessert.classfile.attribute.PermittedSubclassesAttribute;
import de.spricom.dessert.samples.sealed.Apple;
import de.spricom.dessert.samples.sealed.Fruit;
import de.spricom.dessert.samples.sealed.Pear;
import de.spricom.dessert.samples.sealed.Vehicle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PermittedSubclassesTest {

    @Disabled
    @Test
    public void dump() throws IOException {
        var sealedClasses = List.of(Apple.class, Pear.class, Fruit.class,
        Vehicle.class, Vehicle.Car.class, Vehicle.Lorry.class);
        for (var cl : sealedClasses) {
            var cf = new ClassFile(cl);
            System.out.println("*** " + cl.getName() + " ***");
            System.out.println(cf.dump());
        }
    }

    @Test
    public void testFruit() throws IOException {
        ClassFile cf = new ClassFile(Fruit.class);
        PermittedSubclassesAttribute attribute = attribute(cf, PermittedSubclassesAttribute.class);
        Set<String> dependencies = new HashSet();
        attribute.addDependentClassNames(dependencies);
        assertThat(dependencies).containsOnly(Apple.class.getName(), Pear.class.getName());
    }

    @Test
    public void testVehicle() throws IOException {
        ClassFile cf = new ClassFile(Vehicle.class);
        PermittedSubclassesAttribute attribute = attribute(cf, PermittedSubclassesAttribute.class);
        Set<String> dependencies = new HashSet();
        attribute.addDependentClassNames(dependencies);
        assertThat(dependencies).containsOnly(Vehicle.Car.class.getName(), Vehicle.Lorry.class.getName());
    }

    private <A extends AttributeInfo> A attribute(ClassFile cf, Class<A> attributeClass) {
        List<A> attributes = Attributes.filter(cf.getAttributes(), attributeClass);
        assertThat(attributes).as("No unique " + Attributes.attributeName(attributeClass)
                + " in " + cf.getThisClass()).hasSize(1);
        return attributes.get(0);
    }
}
