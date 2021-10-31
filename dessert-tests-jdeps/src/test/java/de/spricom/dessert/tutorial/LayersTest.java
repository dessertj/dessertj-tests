package de.spricom.dessert.tutorial;

import de.spricom.dessert.classfile.attribute.AttributeInfo;
import de.spricom.dessert.slicing.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

public class LayersTest {
    private static final Classpath cp = new Classpath();

    @Test
    void investigateJunitJupiterApi() {
        Root jupiter = cp.rootOf(Test.class);
        SortedMap<String, PackageSlice> packages = jupiter.partitionByPackage();
        permute(new ArrayList<>(packages.keySet())).forEach(p -> {
            if (packages.get(p.getLeft()).uses(packages.get(p.getRight()))) {
                System.out.printf("%-40s -> %s%n", p.getLeft(), p.getRight());
                Slice l = packages.get(p.getLeft());
                Slice r = packages.get(p.getRight());
                for (Clazz clazz : l.slice(c -> c.uses(r)).getClazzes()) {
                    String usages = clazz.getDependencies().slice(r).getClazzes().stream()
                            .map(this::name).collect(Collectors.joining(", "));
                    System.out.printf("  %s uses %s%n", name(clazz), usages);
                }
            }
        });
    }

    @Test
    void investigateJunitEngine() {
        Root jupiter = cp.rootOf(cp.asClazz("org.junit.platform.launcher.Launcher").getRootFile());
        SortedMap<String, PackageSlice> packages = jupiter.partitionByPackage();
        permute(new ArrayList<>(packages.keySet())).forEach(p -> {
            if (packages.get(p.getLeft()).uses(packages.get(p.getRight()))) {
                System.out.printf("%-40s -> %s%n", p.getLeft(), p.getRight());
                Slice l = packages.get(p.getLeft());
                Slice r = packages.get(p.getRight());
                for (Clazz clazz : l.slice(c -> c.uses(r)).getClazzes()) {
                    String usages = clazz.getDependencies().slice(r).getClazzes().stream()
                            .map(this::name).collect(Collectors.joining(", "));
                    System.out.printf("  %s uses %s%n", name(clazz), usages);
                }
            }
        });
    }

    @Test
    void listJunit5() {
        Root junit4 = cp.rootOf(Before.class);
        Slice junit5 = cp.slice("org.junit..*")
                .minus(junit4) // ignore old junit4 classes
                .minus("..shadow..*") // shadow packages don't belong to junit itself
                .minus(this::isDeprecated); // ignore deprecated classes
        dessert(junit5.partitionByPackage()).isCycleFree();
        junit5.getClazzes().stream().sorted().forEach(c -> System.out.printf("%s: %s%n", c.getRootFile().getName(), c.getName()));
    }

    private boolean isDeprecated(Clazz clazz) {
        // using the ClassFile is more efficient than reflection
        for (AttributeInfo attribute : clazz.getClassFile().getAttributes()) {
            if ("Deprecated".equals(attribute.getName())) {
                return true;
            }
        }
        return false;
    }

    private String name(Clazz c) {
        return c.getName().substring(c.getPackageName().length() + 1);
    }

    private <X> Stream<Pair<X, X>> permute(List<X> list) {
        return pairs(list).flatMap(p -> Stream.of(p, Pair.of(p.getRight(), p.getLeft())));
    }

    private <X> Stream<Pair<X, X>> pairs(List<X> list) {
        int sz = list.size();
        if (sz < 2) {
            throw new IllegalArgumentException("sz = " + sz);
        }
        if (sz == 2) {
            return Stream.of(Pair.of(list.get(0), list.get(1)));
        }
        X first = list.get(0);
        Stream<Pair<X, X>> pairs = IntStream.range(1, sz)
                .mapToObj(list::get)
                .map(r -> Pair.of(first, r));
        return Stream.concat(pairs, pairs(list.subList(1, sz)));
    }

}
