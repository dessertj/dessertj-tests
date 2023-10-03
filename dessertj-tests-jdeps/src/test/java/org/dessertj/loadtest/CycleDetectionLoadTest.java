package org.dessertj.loadtest;

import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Clazz;
import org.dessertj.slicing.PackageSlice;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;

import static org.dessertj.assertions.SliceAssertions.dessert;

// [WARNING] Tests run: 3, Failures: 0, Errors: 0, Skipped: 1, Time elapsed: 2,453.105 s - in org.dessertj.loadtest.CycleDetectionLoadTest
public class CycleDetectionLoadTest {

    @Test
    public void testPackages() {
        SortedMap<String, PackageSlice> packages = new Classpath().partitionByPackage();
        detectCycles(packages.values(), "packages");
    }

    @Disabled
    @Test
    public void dumpCycleCause() {
        Classpath cp = new Classpath();
        dessert(cp.packageOf("net.bytebuddy.implementation.bytecode.assign.primitive"))
                .usesNot(cp.packageOf("net.bytebuddy.implementation"));
    }

//Cycle detection for 74726 classes needed 2378.898933.2 Sec. and found:
//Cycle detected:
//org.springframework.restdocs.mustache.Mustache$Delims -> org.springframework.restdocs.mustache.Mustache
//org.springframework.restdocs.mustache.Mustache -> org.springframework.restdocs.mustache.Mustache$Delims

    @Test
    public void testClasses() {
        Set<Clazz> classes = new Classpath().getClazzes();
        detectCycles(classes, "classes");
    }

//Cycle detection for 3773 packages needed 89.818955.2 Sec. and found:
//Cycle detected:
//java.lang -> java.net:
//        Class -> URL
//        ClassLoader -> URL
//        Module -> URI, URL
//        NamedPackage -> URI
//        Package -> MalformedURLException, URI, URL
//        Package$VersionInfo -> URL
//        SecurityManager -> InetAddress, SocketPermission
//        System -> URL
//        System$2 -> URI
//java.net -> jdk.internal.vm.annotation:
//        InetAddress -> Stable
//jdk.internal.vm.annotation -> java.lang:
//        ChangesCurrentThread -> Object
//        Contended -> Object, String
//        DontInline -> Object
//        ForceInline -> Object
//        Hidden -> Object
//        IntrinsicCandidate -> Object
//        JvmtiMountTransition -> Object
//        ReservedStackAccess -> Object
//        Stable -> Object
    private void detectCycles(Collection<? extends Slice> slices, String name) {
        long ts = System.nanoTime();
        try {
            dessert(slices).isCycleFree();
        } catch (AssertionError er) {
            System.out.printf("Cycle detection for %d %s needed %f Sec. and found:%n%s",
                    slices.size(), name,
                    (System.nanoTime() - ts) / 1e9,
                    er.getMessage());
        }
    }
}
