package org.dessertj.samples;

import org.dessertj.assertions.SliceAssertions;
import org.dessertj.partitioning.ClazzPredicates;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.PackageSlice;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Test;

import java.util.SortedMap;

import static org.dessertj.assertions.SliceAssertions.dessert;
import static org.assertj.core.api.Assertions.assertThat;

public class DessertGroupSampleTest {
    private final Classpath cp = new Classpath();
    private final Slice dessert = cp.rootOf(Slice.class).minus(ClazzPredicates.DEPRECATED);

    @Test
    void testPackagesNotEmpty() {
        assertThat(dessert.partitionByPackage()).isNotEmpty();
    }

    @Test
    public void testCycleFree() {
        dessert(dessert.partitionByPackage()).isCycleFree();
    }

    @Test
    public void testNestingRuleNoParentPackage() {
        SortedMap<String, PackageSlice> packages = dessert.partitionByPackage();

        packages.forEach((name, pckg) -> dessert(pckg).usesNot(pckg.getParentPackage()));
    }

    @Test
    public void testNestingRuleNoAncestorPackage() {
        SortedMap<String, PackageSlice> packages = dessert.partitionByPackage();

        packages.forEach((name, pckg) -> SliceAssertions.dessert(pckg)
                .usesNot(dessert.slice(clazz -> pckg.getParentPackageName().startsWith(clazz.getPackageName()))));
    }
}
