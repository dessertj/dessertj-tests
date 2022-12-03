package de.spricom.dessert.samples.modules;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.modules.ModuleRegistry;
import de.spricom.dessert.modules.core.ModuleSlice;
import de.spricom.dessert.modules.fixed.JavaModules;
import de.spricom.dessert.modules.fixed.JdkModules;
import de.spricom.dessert.samples.modules.illegal.UseInternal;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.slicing.Slices;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;

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
        if (false) {
            new TreeSet<>(mr.getModuleNames()).forEach(n -> System.out.printf("\"%s\",%n", n));
        }
        assertThat(mr.getModuleNames())
                .contains(
                        "java.base",
                        "java.compiler",
                        "java.datatransfer",
                        "java.desktop",
                        "java.instrument",
                        "java.logging",
                        "java.management",
                        "java.management.rmi",
                        "java.naming",
                        "java.net.http",
                        "java.prefs",
                        "java.rmi",
                        "java.scripting",
                        "java.se",
                        "java.security.jgss",
                        "java.security.sasl",
                        "java.smartcardio",
                        "java.sql",
                        "java.sql.rowset",
                        "java.transaction.xa",
                        "java.xml",
                        "java.xml.crypto",
                        "jdk.accessibility",
                        "jdk.attach",
                        "jdk.charsets",
                        "jdk.compiler",
                        "jdk.crypto.cryptoki",
                        "jdk.crypto.ec",
                        "jdk.crypto.mscapi",
                        "jdk.dynalink",
                        "jdk.editpad",
                        "jdk.hotspot.agent",
                        "jdk.httpserver",
                        "jdk.incubator.vector",
                        "jdk.internal.ed",
                        "jdk.internal.jvmstat",
                        "jdk.internal.le",
                        "jdk.internal.opt",
                        "jdk.internal.vm.ci",
                        "jdk.internal.vm.compiler",
                        "jdk.internal.vm.compiler.management",
                        "jdk.jartool",
                        "jdk.javadoc",
                        "jdk.jcmd",
                        "jdk.jconsole",
                        "jdk.jdeps",
                        "jdk.jdi",
                        "jdk.jdwp.agent",
                        "jdk.jfr",
                        "jdk.jlink",
                        "jdk.jpackage",
                        "jdk.jshell",
                        "jdk.jsobject",
                        "jdk.jstatd",
                        "jdk.localedata",
                        "jdk.management",
                        "jdk.management.agent",
                        "jdk.management.jfr",
                        "jdk.naming.dns",
                        "jdk.naming.rmi",
                        "jdk.net",
                        "jdk.nio.mapmode",
                        "jdk.random",
                        "jdk.sctp",
                        "jdk.security.auth",
                        "jdk.security.jgss",
                        "jdk.unsupported",
                        "jdk.unsupported.desktop",
                        "jdk.xml.dom",
                        "jdk.zipfs",
                        "org.apiguardian.api",
                        "org.assertj.core",
                        "org.junit.jupiter.api",
                        "org.junit.jupiter.engine",
                        "org.junit.platform.commons",
                        "org.junit.platform.engine",
                        "org.opentest4j"
                );
    }

    @Test
    void testUsages() {
        Slice useInternal = cp.sliceOf(UseInternal.class);
        Slice thisPackage = cp.packageTreeOf(this.getClass()).minus(useInternal);
        dessert(thisPackage).doesNotUse(java.management.rmi, java.compiler, jdk.compiler);
        dessert(thisPackage).usesOnly(java.base, junit, assertj, dessert, useInternal);
    }

    @Disabled("will fail")
    @Test
    void usagesWithInternal() {
        Slice thisPackage = cp.packageTreeOf(this.getClass());
        dessert(thisPackage).usesOnly(java.base, junit, assertj, dessert);
    }

    @Test
    void testUsagesWithInternalFailure() {
        try {
            usagesWithInternal();
        } catch (AssertionError er) {
            assertThat(er.toString()).isEqualTo("""
                    java.lang.AssertionError: Illegal Dependencies:
                    de.spricom.dessert.samples.modules.illegal.UseInternal
                     -> jdk.internal.util.jar.JarIndex
                    """);
        }
    }

    @Test
    void testNoInternalClasses() {
        Slice internals = Slices.of(mr.getModules().stream().map(ModuleSlice::getInternals).toList());
        dessert(cp.packageOf(this.getClass())).doesNotUse(internals);
    }

    @Disabled("will fail")
    @Test
    void detectUsageOfInternalClass() {
        Slice internals = Slices.of(mr.getModules().stream().map(ModuleSlice::getInternals).toList());
        dessert(cp.packageTreeOf(this.getClass())).doesNotUse(internals);
    }

    @Test
    void testDetectUsageOfInternalClassFailure() {
        try {
            detectUsageOfInternalClass();
        } catch (AssertionError er) {
            assertThat(er.toString()).isEqualTo("""
                    java.lang.AssertionError: Illegal Dependencies:
                    de.spricom.dessert.samples.modules.illegal.UseInternal
                     -> jdk.internal.util.jar.JarIndex
                    """);
        }
    }
}
