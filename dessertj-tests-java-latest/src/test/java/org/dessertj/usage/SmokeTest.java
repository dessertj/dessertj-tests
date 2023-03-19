package org.dessertj.usage;

import org.dessertj.modules.ModuleRegistry;
import org.dessertj.modules.core.ModuleSlice;
import org.dessertj.modules.fixed.JavaModules;
import org.dessertj.resolve.ClassResolver;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import static org.dessertj.assertions.SliceAssertions.dessert;


public class SmokeTest {
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final Classpath cp = new Classpath();
    private static final ModuleRegistry mr = new ModuleRegistry(cp);
    private static final JavaModules java = new JavaModules(mr);

    @BeforeEach
    void setUp(TestInfo info) {
        log.info(() -> "Running " + info.getDisplayName());
    }

    @Test
    void checkLog4j() {
        ModuleSlice junit = mr.getModule("org.junit.jupiter.api");
        Slice dessert = cp.rootOf(Slice.class);

        dessert(cp.sliceOf(this.getClass())).usesOnly(java.base, java.logging, junit, dessert);
    }

    @Test
    void checkOldVariant() {
        Classpath oldcp = new Classpath(ClassResolver.ofClassPathAndJavaRuntime());

        Slice junit = oldcp.rootOf(Test.class);
        Slice dessert = oldcp.rootOf(Slice.class);
        Slice java = oldcp.slice("java.lang|util..*");

        dessert(oldcp.sliceOf(this.getClass())).usesOnly(java, junit, dessert);
    }
}
