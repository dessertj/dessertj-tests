package dessertj.tests.java21;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaConstant;
import org.dessertj.classfile.ClassFile;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * See <a href="https://www.javacodegeeks.com/2018/08/hands-on-java-constantdynamic.html">Hands on Java 11’s constantdynamic</a>
 */
public class ConstantDynamicTest {

    @Test
    void invokeCallableWithContantDynamic() throws Exception {
        DynamicType.Unloaded<Callable> unloaded = createUnloadedCallable();
        Constructor<? extends Callable> loaded = unloaded
                .load(ConstantDynamicSample.class.getClassLoader())
                .getLoaded()
                .getConstructor();

        Callable<?> first = loaded.newInstance();
        Callable<?> second = loaded.newInstance();
        assertThat(first.call()).isSameAs(second.call());
        assertThat(ConstantDynamicSample.getInstanceCount()).isEqualTo(1);
    }

    private DynamicType.Unloaded<Callable> createUnloadedCallable() throws NoSuchMethodException {
        return new ByteBuddy()
                .subclass(Callable.class)
                .method(ElementMatchers.named("call"))
                .intercept(FixedValue.value(JavaConstant.Dynamic.ofInvocation(ConstantDynamicSample.class.getConstructor())))
                .make();
    }

    @Test
    void unloadedClassContainsConstantDynamic() throws IOException, NoSuchMethodException {
        ClassFile cf = new ClassFile(new ByteArrayInputStream(createUnloadedCallable().getBytes()));
        String dump = cf.dumpConstantPool();
        System.out.println(dump);
        assertThat(dump).contains(": Dynamic ");
        assertThat(dump).contains("// [bootstrapMethodAttrIndex=0]._: dessertj.tests.java21.ConstantDynamicSample");
    }
}
