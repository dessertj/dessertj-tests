package org.dessertj.samples.modules;

import org.dessertj.assertions.SliceAssertions;
import org.dessertj.modules.ModuleRegistry;
import org.dessertj.modules.core.ModuleSlice;
import org.dessertj.modules.fixed.JavaModules;
import org.dessertj.modules.fixed.JdkModules;
import org.dessertj.samples.modules.illegal.UseInternal;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Slice;
import org.dessertj.slicing.Slices;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static org.dessertj.assertions.SliceAssertions.dessert;
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
            int count = 1;
            for (String name : new TreeSet<>(mr.getModuleNames())) {
                System.out.printf("\"%s\", // %d%n", name, count++);
            }
        }
        assertThat(mr.getModuleNames())
                .hasSizeBetween(84, 86) // 84 on Linux, 86 on Windows
                .contains(
                        "java.base", // 4
                        "java.compiler", // 5
                        "java.datatransfer", // 6
                        "java.desktop", // 7
                        "java.instrument", // 8
                        "java.logging", // 9
                        "java.management", // 10
                        "java.management.rmi", // 11
                        "java.naming", // 12
                        "java.net.http", // 13
                        "java.prefs", // 14
                        "java.rmi", // 15
                        "java.scripting", // 16
                        "java.se", // 17
                        "java.security.jgss", // 18
                        "java.security.sasl", // 19
                        "java.smartcardio", // 20
                        "java.sql", // 21
                        "java.sql.rowset", // 22
                        "java.transaction.xa", // 23
                        "java.xml", // 24
                        "java.xml.crypto", // 25
                        "jdk.accessibility",
                        "jdk.attach",
                        "jdk.charsets",
                        "jdk.compiler",
                        "jdk.crypto.cryptoki",
                        "jdk.crypto.ec",
                        // "jdk.crypto.mscapi", (only available on Windows)
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
                        "jdk.sctp",
                        "jdk.security.auth",
                        "jdk.security.jgss",
                        "jdk.unsupported",
                        "jdk.unsupported.desktop",
                        "jdk.xml.dom",
                        "jdk.zipfs",
                        "org.apiguardian.api",
                        "org.assertj.core",
                        "org.dessertj.core",
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
                    org.dessertj.samples.modules.illegal.UseInternal
                     -> org.dessertj.classfile.constpool.ConstantPool
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
                    org.dessertj.samples.modules.illegal.UseInternal
                     -> org.dessertj.classfile.constpool.ConstantPool
                    """);
        }
    }
}
