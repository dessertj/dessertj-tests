package de.spricom.dessert.usage;

import de.spricom.dessert.modules.ModuleRegistry;
import de.spricom.dessert.modules.core.ModuleSlice;
import de.spricom.dessert.modules.fixed.JavaModules;
import de.spricom.dessert.resolve.ClassResolver;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;


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
        Classpath oldcp = new Classpath(ClassResolver.ofClassPathAndBootClassPath());

        Slice junit = oldcp.rootOf(Test.class);
        Slice dessert = oldcp.rootOf(Slice.class);
        Slice java = oldcp.slice("java.lang|util..*");

        dessert(oldcp.sliceOf(this.getClass())).usesOnly(java, junit, dessert);
    }
}
