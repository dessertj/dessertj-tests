package de.spricom.dessert.samples.modules;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.modules.ModuleRegistry;
import de.spricom.dessert.modules.core.ModuleSlice;
import de.spricom.dessert.modules.fixed.JavaModules;
import de.spricom.dessert.modules.fixed.JdkModules;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Slice;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;
import static org.assertj.core.api.Assertions.assertThat;

public class JavaModulesTest {
    private final Classpath cp = new Classpath();
    private final ModuleRegistry mr = new ModuleRegistry(cp);
    private final JavaModules java = new JavaModules(mr);
    private final JdkModules jdk = new JdkModules(mr);
    private final ModuleSlice junit = mr.getModule("org.junit.jupiter.api");
    private final Slice assertj = cp.rootOf(Assertions.class);
    private final Slice dessert = cp.rootOf(SliceAssertions.class);

    @Test
    void testListModules() {
        assertThat(mr.getModuleNames())
                .contains("java.base", "org.junit.jupiter.api")
                .hasSize(78);
    }

    @Test
    void testUsages() {
        Slice thisPackage = cp.packageTreeOf(this.getClass());
        dessert(thisPackage).doesNotUse(java.management.rmi, java.compiler, jdk.compiler);
        dessert(thisPackage).usesOnly(java.base, junit, assertj, dessert);
    }
}
