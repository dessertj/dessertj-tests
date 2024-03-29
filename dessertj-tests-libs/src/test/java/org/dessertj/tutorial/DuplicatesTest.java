package org.dessertj.tutorial;

import org.dessertj.classfile.ClassFile;
import org.dessertj.classfile.FieldInfo;
import org.dessertj.classfile.MethodInfo;
import org.dessertj.classfile.attribute.AttributeInfo;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Clazz;
import org.dessertj.slicing.Root;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class DuplicatesTest {
    private static final Classpath cp = new Classpath();

    @Disabled("will fail")
    @Test
    @DisplayName("Detect duplicates")
    void detectDuplicates() {
        Slice duplicates = cp.duplicates().minus("module-info");

        List<File> duplicateJars = duplicates.getClazzes().stream()
                .map(Clazz::getRoot)
                .map(Root::getRootFile)
                .distinct()
                .sorted(Comparator.comparing(File::getName))
                .toList();

        Map<String, Set<Root>> duplicateJarsByClass = duplicates.getClazzes().stream()
                .collect(Collectors.groupingBy(Clazz::getName,
                        TreeMap::new,
                        Collectors.mapping(Clazz::getRoot, Collectors.toSet())));

        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            pw.printf("%nThere are %d duplicate classes spread over %d jars:%n",
                    duplicateJarsByClass.size(), duplicateJars.size());
            pw.println("\nDuplicate classes:");
            duplicateJarsByClass.forEach((name, files) -> pw.printf("%s (%s)%n", name,
                    files.stream()
                            .map(Root::getRootFile)
                            .map(File::getName)
                            .sorted()
                            .collect(Collectors.joining(", "))));
            pw.println("\nJARs containing duplicates:");
            duplicateJars.forEach(jar -> pw.printf("%s%n", jar.getName()));
        }

        assertThat(duplicates.getClazzes().size()).as(sw.toString()).isEqualTo(0);
    }

    @Test
    @DisplayName("Make sure there are no additional duplicates")
    void ensureNoAdditonalDuplicates() {
        Slice duplicates = cp.duplicates().minus("module-info");

        List<File> duplicateJars = duplicates.getClazzes().stream()
                .map(Clazz::getRoot)
                .map(Root::getRootFile)
                .distinct()
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());

        Map<String, Set<Root>> duplicateJarsByClass = duplicates.getClazzes().stream()
                .collect(Collectors.groupingBy(Clazz::getName,
                        TreeMap::new,
                        Collectors.mapping(Clazz::getRoot, Collectors.toSet())));

        System.out.printf("There are %d duplicate classes spread over %d jars:%n",
                duplicateJarsByClass.size(), duplicateJars.size());
        System.out.println("\nDuplicate classes:");
        duplicateJarsByClass.forEach((name, files) -> System.out.printf("%s (%s)%n", name,
                files.stream()
                        .map(Root::getRootFile)
                        .map(File::getName)
                        .sorted()
                        .collect(Collectors.joining(", "))));
        System.out.println("\nJARs containing duplicates:");
        duplicateJars.forEach(jar -> System.out.printf("%s%n", jar.getName()));

        // make sure there are no jars involved
        assertThat(duplicateJars).isEmpty();

        // make sure there are no classes involved
        assertThat(duplicates.getClazzes()).isEmpty();
    }

    @Test
    @DisplayName("Dump all duplicates for which the .class files are different")
    void dumpBinaryDifferences() {
        Slice duplicates = cp.duplicates().minus("module-info");

        Map<String, List<Clazz>> duplicatesByName = duplicates.getClazzes().stream()
                .collect(Collectors.groupingBy(Clazz::getName));

        for (List<Clazz> list : duplicatesByName.values()) {
            list.subList(1, list.size()).forEach(c -> checkBinaryContent(list.get(0), c));
        }
    }

    private void checkBinaryContent(Clazz c1, Clazz c2) {
        if (!isSameBinaryContent(c1, c2)) {
            System.out.println("Binaries of " + c1.getURI() + " and " + c2.getURI() + " are different.");
        }
    }

    private boolean isSameBinaryContent(Clazz c1, Clazz c2) {
        try (InputStream is1 = c1.getURI().toURL().openStream();
             InputStream is2 = c2.getURI().toURL().openStream()) {
            byte[] bin1 = is1.readAllBytes();
            byte[] bin2 = is2.readAllBytes();
            return Arrays.equals(bin1, bin2);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot compare duplicates of " + c1.getName());
        }
    }

    @Test
    @DisplayName("Dump all duplicates for which the API differs")
    void dumpApiDifferences() {
        Slice duplicates = cp.duplicates().minus("module-info");

        Map<String, List<Clazz>> duplicatesByName = duplicates.getClazzes().stream()
                .collect(Collectors.groupingBy(Clazz::getName));

        for (List<Clazz> list : duplicatesByName.values()) {
            list.subList(1, list.size()).forEach(c -> checkAPI(list.get(0), c));
        }
    }

    private void checkAPI(Clazz c1, Clazz c2) {
        if (!isSameAPI(c1, c2)) {
            System.out.println("API of " + c1.getURI() + " and " + c2.getURI() + " are different.");
        }
    }

    private boolean isSameAPI(Clazz c1, Clazz c2) {
        ClassFile cf1 = c1.getClassFile();
        ClassFile cf2 = c2.getClassFile();
        return cf1.getAccessFlags() == cf2.getAccessFlags()
                && cf1.getThisClass().equals(cf2.getThisClass())
                && cf1.getSuperClass().equals(cf2.getSuperClass())
                && Arrays.equals(cf1.getInterfaces(), cf2.getInterfaces())
                && isEqual(cf1.getFields(), cf2.getFields(), this::isEqual)
                && isEqual(cf1.getMethods(), cf2.getMethods(), this::isEqual)
                && isEqual(cf1.getAttributes(), cf2.getAttributes(), this::isEqual);
    }

    private <T> boolean isEqual(T[] t1, T[] t2, BiPredicate<T, T> predicate) {
        if (t1 == null && t2 == null) {
            return true;
        }
        if (t1 == null || t2 == null) {
            return false;
        }
        if (t1.length != t2.length) {
            return false;
        }
        for (int i = 0; i < t1.length; i++) {
            if (!predicate.test(t1[i], t2[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isEqual(MethodInfo m1, MethodInfo m2) {
        return m1.getAccessFlags() == m2.getAccessFlags()
                && m1.getDeclaration().equals(m2.getDeclaration())
                && isEqual(m1.getAttributes(), m2.getAttributes(), this::isEqual);
    }

    private boolean isEqual(FieldInfo f1, FieldInfo f2) {
        return f1.getAccessFlags() == f2.getAccessFlags()
                && f1.getDeclaration().equals(f2.getDeclaration())
                && isEqual(f1.getAttributes(), f2.getAttributes(), this::isEqual);
    }

    private boolean isEqual(AttributeInfo a1, AttributeInfo a2) {
        return a1.getName().equals(a2.getName())
                && a1.getContext() == a2.getContext();
    }
}
