package org.dessertj.samples;

import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.ConcreteSlice;
import org.dessertj.slicing.PackageSlice;
import org.dessertj.slicing.Slice;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.SortedMap;

import static org.dessertj.assertions.SliceAssertions.dessert;

class SpringTest {

    private final Classpath cp = new Classpath();
    private final Slice springframework = cp.packageTreeOf("org.springframework..*");

    @Test
    void checkPackageCycles() {
        SortedMap<String, PackageSlice> packages = springframework
                // remove known cycles
                .minus("org.springframework.cglib|objenesis|boot|batch|data|test..*")
                .minus("org.springframework.security.config..*")
                .minus("org.springframework.security.authorization.event..*")
                .partitionByPackage();
        Assertions.assertThat(packages).hasSizeGreaterThan(10);
        dessert(packages).isCycleFree();
    }

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