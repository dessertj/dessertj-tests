package org.dessertj.samples;

import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.ConcreteSlice;
import org.dessertj.slicing.PackageSlice;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.SortedMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dessertj.assertions.SliceAssertions.dessert;

class SpringTest {

    private final Classpath cp = new Classpath();
    private final Slice springframework = cp.packageTreeOf("org.springframework..*");

    @Test
    void checkPackageCycles() {
        SortedMap<String, PackageSlice> packages = springframework
                // remove known cycles
                .minus("org.springframework.cglib|objenesis|batch|boot|data|test|aop..*")
                .minus("org.springframework.http.codec|server..*")
                .minus("org.springframework.security.authorization|config..*")
                .partitionByPackage();
        assertThat(packages).hasSizeGreaterThan(350);
        dessert(packages).isCycleFree();
    }


    @Disabled("will fail, because there are cycles")
    @Test
    void allCycles() {
        dessert(springframework.partitionByPackage()).isCycleFree();
    }

    /**
     * This Clazz ist a static private inner record with signature 'TV;'
     */
    @Test
    void testConcurrentLruCacheCacheEntry() {
        Slice sample = cp.slice("org.springframework.util.ConcurrentLruCache$CacheEntry");
        assertThat(sample.getClazzes()).hasSize(1);
        assertThat(sample.getDependencies().getClazzes()).isNotEmpty();
    }

    @Test
    void testMethodDescriptor() {
        Slice sample = cp.asClazz("org.springframework.beans.factory.support.MethodDescriptor");
        assertThat(sample.getClazzes()).hasSize(1);
        assertThat(sample.getDependencies().getClazzes()).isNotEmpty();
    }

    // org.springframework.beans.factory.support.MethodDescriptor

    @Test
    void showUsageOfNestedPackagesFromOuterPackages() {
        SortedMap<String, PackageSlice> packages = cp.rootOf(ApplicationContext.class).partitionByPackage();
        for (PackageSlice slice : packages.values()) {
            try {
                dessert(slice).usesNot(slice.getParentPackage());
            } catch (AssertionError ae) {
                System.out.println(ae.getMessage());
            }
        }
    }

    @Test
    void showUsageOfOuterPackagesByNestedPackages() {
        SortedMap<String, PackageSlice> packages = cp.rootOf(ApplicationContext.class).partitionByPackage();
        for (PackageSlice slice : packages.values()) {
            try {
                dessert(slice.getParentPackage()).usesNot(slice);
            } catch (AssertionError ae) {
                System.out.println(ae.getMessage());
            }
        }
    }

    @Test
    void listDuplicates() {
        Classpath cp = new Classpath();
        ConcreteSlice duplicates = cp.duplicates();
        duplicates.getClazzes().forEach(clazz -> System.out.println(clazz.getURI()));
    }

}
