package de.spricom.dessert.tests.classfile;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.classfile.attribute.AttributeInfo;
import de.spricom.dessert.classfile.attribute.NestHostAttribute;
import de.spricom.dessert.classfile.attribute.NestMembersAttribute;
import de.spricom.dessert.samples.nesting.Car;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class NestingTest {

    @Test
    public void testCar() {
        Car car = new Car();
        car.drive();
    }

    @Disabled
    @Test
    public void dumpCarClassfile() throws IOException {
        ClassFile cf = new ClassFile(Car.class);
        System.out.println(cf.dump());
    }

    @Disabled
    @Test
    public void dumpNestedCarClassfile() throws IOException {
        ClassFile cf = new ClassFile(Car.Engine.Carburetor.class);
        System.out.println(cf.dump());
    }

    @Disabled
    @Test
    public void dumpNestMembers() throws IOException {
        ClassFile cf = new ClassFile(Car.class);
        Optional<AttributeInfo> nestMembers = Arrays.stream(cf.getAttributes())
                .filter(attributeInfo -> "NestMembers".equals(attributeInfo.getName()))
                .findAny();
        assertThat(nestMembers).isPresent();
        NestMembersAttribute nest = (NestMembersAttribute) nestMembers.get();
        for (String member : nest.getMembers()) {
            String resource = "/" + member.replace('.', '/') + ".class";
            try (InputStream is = Car.class.getResourceAsStream(resource)) {
                ClassFile memberCF = new ClassFile(is);
                System.out.println();
                System.out.println("*** " + member + " ***");
                System.out.println(memberCF.dump());
            }
        }
    }

    @Test
    public void testNestMembers() throws IOException {
        ClassFile cf = new ClassFile(Car.class);
        NestMembersAttribute nestMembers = attribute(cf, "NestMembers", NestMembersAttribute.class);
        for (String member : nestMembers.getMembers()) {
            String resource = "/" + member.replace('.', '/') + ".class";
            try (InputStream is = Car.class.getResourceAsStream(resource)) {
                ClassFile memberCF = new ClassFile(is);
                NestHostAttribute nestHost = attribute(memberCF, "NestHost", NestHostAttribute.class);
                assertThat(nestHost.getHostClassName()).isEqualTo(Car.class.getName());
            }
        }
    }

    private <A extends AttributeInfo> A attribute(ClassFile cf, String attributeName, Class<A> attributeClass) {
        List<A> attributes = Arrays.stream(cf.getAttributes())
                .filter(attributeInfo -> attributeName.equals(attributeInfo.getName()))
                .map(attributeClass::cast)
                .collect(Collectors.toList());
        assertThat(attributes).as("No " + attributeName + " in " + cf.getThisClass()).hasSize(1);
        return attributes.get(0);
    }
}
