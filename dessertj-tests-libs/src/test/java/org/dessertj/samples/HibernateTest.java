package org.dessertj.samples;

import org.dessertj.slicing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dessertj.assertions.SliceAssertions.dessert;


public class HibernateTest {
    private final Classpath cp = new Classpath();
    private Slice hibernate;

    @BeforeEach
    public void init() {
        List<Slice> hibernateRoots =
                Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                        .filter(name -> name.contains("hibernate"))
                        .peek(System.out::println)
                        .map(name -> cp.rootOf(new File(name)))
                        .collect(Collectors.toList());
        hibernate = Slices.of(hibernateRoots);
    }

    @Test
    public void findPackagesCycle() {
        SortedMap<String, PackageSlice> packages = hibernate.partitionByPackage();
        assertThat(packages).hasSizeGreaterThan(10);
        try {
            dessert(packages).isCycleFree();
            throw new IllegalStateException("No cycle found"); // Cannot use AssertionError here.
        } catch (AssertionError er) {
            System.out.println(er.getMessage());
        }
    }

    @Test
    public void findClassesCycle() {
        Set<Clazz> clazzes = hibernate.getClazzes();
        assertThat(clazzes).hasSizeGreaterThan(1000);
        try {
            dessert(clazzes).isCycleFree();
            throw new IllegalStateException("No cycle found"); // Cannot use AssertionError here.
        } catch (AssertionError er) {
            System.out.println(er.getMessage());
        }
    }
}
