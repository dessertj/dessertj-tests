package org.dessertj.usage;

import org.dessertj.slicing.Classpath;
import org.junit.jupiter.api.Test;

public class ClasspathPerformanceTest {

    @Test
    void test() {
        Classpath cp = new Classpath();
        cp.slice("module-info").getClazzes()
                .forEach(cl -> System.out.println(cl.getRoot().getURI()));
    }
}
