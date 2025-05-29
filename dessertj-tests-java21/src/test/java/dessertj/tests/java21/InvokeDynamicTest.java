package dessertj.tests.java21;

import org.dessertj.classfile.ClassFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class InvokeDynamicTest {

    @Test
    void sampleContainsInvokeDynamic() throws IOException {
        ClassFile cf = new ClassFile(InvokeDynamicSample.class);
        String dump = cf.dumpConstantPool();
        System.out.println(dump);
        assertThat(dump).contains(": InvokeDynamic ");
        assertThat(dump).contains("// [bootstrapMethodAttrIndex=0].run: () -> java.lang.Runnable");
    }
}
