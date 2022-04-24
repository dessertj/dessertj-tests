package de.spricom.dessert.tests.classfile;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.classfile.attribute.*;
import de.spricom.dessert.samples.nesting.Car;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class NestMembersNestHostTest {

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

    @Disabled
    @Test
    void dumpNesting() {
        Classpath cp = new Classpath();
        cp.packageOf(Car.class).getClazzes().forEach(this::dump);
    }
    private void dump(Clazz clazz) {
        ClassFile classFile = clazz.getClassFile();
        System.out.println("\n=== " + classFile.getThisClass() + " ===");
        List<NestMembersAttribute> membersAttributes = Attributes.filter(classFile.getAttributes(), NestMembersAttribute.class);
        if (!membersAttributes.isEmpty()) {
            System.out.println("members:");
            for (NestMembersAttribute nestMembersAttribute : membersAttributes) {
                for (String member : nestMembersAttribute.getMembers()) {
                    System.out.println("\t" + member);
                }
            }
        }
        List<NestHostAttribute> hostAttributes = Attributes.filter(classFile.getAttributes(), NestHostAttribute.class);
        for (NestHostAttribute hostAttribute : hostAttributes) {
            System.out.println("host: " + hostAttribute.getHostClassName());
        }
        List<InnerClassesAttribute> innerClassesAttributes = Attributes.filter(classFile.getAttributes(), InnerClassesAttribute.class);
        for (InnerClassesAttribute innerClassesAttribute : innerClassesAttributes) {
            for (InnerClass innerClass : innerClassesAttribute.getInnerClasses()) {
                System.out.println("inner-class: " + innerClass);
            }
        }
        List<EnclosingMethodAttribute> enclosingMethodAttributes = Attributes.filter(classFile.getAttributes(), EnclosingMethodAttribute.class);
        for (EnclosingMethodAttribute enclosingMethodAttribute : enclosingMethodAttributes) {
            System.out.println(enclosingMethodAttribute);
        }
    }

    @Test
    public void testNestMembers() throws IOException {
        ClassFile cf = new ClassFile(Car.class);
        NestMembersAttribute nestMembers = attribute(cf, NestMembersAttribute.class);
        for (String member : nestMembers.getMembers()) {
            String resource = "/" + member.replace('.', '/') + ".class";
            try (InputStream is = Car.class.getResourceAsStream(resource)) {
                ClassFile memberCF = new ClassFile(is);
                NestHostAttribute nestHost = attribute(memberCF, NestHostAttribute.class);
                assertThat(nestHost.getHostClassName()).isEqualTo(Car.class.getName());
            }
        }
    }

    private <A extends AttributeInfo> A attribute(ClassFile cf, Class<A> attributeClass) {
        List<A> attributes = Attributes.filter(cf.getAttributes(), attributeClass);
        assertThat(attributes).as("No unique " + Attributes.attributeName(attributeClass)
                + " in " + cf.getThisClass()).hasSize(1);
        return attributes.get(0);
    }
}
