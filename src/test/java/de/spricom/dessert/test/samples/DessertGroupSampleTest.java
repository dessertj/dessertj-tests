package de.spricom.dessert.test.samples;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.groups.PackageSlice;
import de.spricom.dessert.groups.SliceGroup;
import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.slicing.SliceContext;
import org.junit.Test;

import java.io.IOException;

public class DessertGroupSampleTest {

    @Test
    public void testCycleFree() throws IOException {
        Slice slice = new SliceContext().packageTreeOf("de.spricom.dessert");

        SliceGroup<PackageSlice> sg = SliceGroup.splitByPackage(slice);
        SliceAssertions.dessert(sg).isCycleFree();

        // short form:
        SliceAssertions.dessert(slice).splitByPackage().isCycleFree();
    }

    @Test
    public void testPackageDependencies() throws IOException {
        Slice slice = new SliceContext().packageTreeOf("de.spricom.dessert");
        SliceGroup<PackageSlice> packages = SliceGroup.splitByPackage(slice);

        packages.forEach(pckg -> SliceAssertions.assertThat(pckg)
                .doesNotUse(pckg.getParentPackage(packages)));

    }
}