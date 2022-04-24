package de.spricom.dessert.tutorial;

import de.spricom.dessert.classfile.attribute.AttributeInfo;
import de.spricom.dessert.slicing.*;
import de.spricom.dessert.util.CombinationUtils;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

public class CyclesTest {
    private static final Classpath cp = new Classpath();

    @Test
    void investigateSpringFrameworkPackageCycles() {
        Slice springframework = cp.slice("org.springframework..*")
                .minus("org.springframework.cglib|objenesis|boot|batch|data|test..*")
                .minus("org.springframework.security.config..*");
        SortedMap<String, PackageSlice> packages = springframework.partitionByPackage();
        dessert(packages).isCycleFree();
    }

    @Test
    void investigateSpringBatchInfrastructureCycles() {
        SortedMap<String, PackageSlice> packages = cp.rootOf(ItemReader.class)
                .minus(this::isDeprecated)
                .partitionByPackage();
        Map<String, Slice> mergedPackages = new HashMap<>(packages);

        List<Stream<String>> cycles = List.of(
                Stream.of("item", "item.util"),
                Stream.of("item.file", "item.support")
        );

        int i = 0;
        for (Stream<String> involvedPackages : cycles) {
            List<Slice> cycle = involvedPackages
                    .map("org.springframework.batch."::concat)
                    .map(mergedPackages::remove).collect(Collectors.toList());
            i++;
            System.out.printf("%n----- CYCLE %d ------------------------------------------------%n", i);
            investigateCycle(cycle);
            mergedPackages.put("cycle" + i, Slices.of(cycle).named("cycle" + i));
        }

        dessert(mergedPackages).isCycleFree();
    }

    private void investigateCycle(List<Slice> slices) {
        investigateCycle(slices, c -> c.getName().substring(c.getPackageName().length() + 1));
    }

    private void investigateCycle(List<Slice> slices, Function<Clazz, String> name) {
        CombinationUtils.combinations(slices).forEach(p -> {
            Slice l = p.getLeft();
            Slice r = p.getRight();
            if (l.uses(r)) {
                System.out.printf("\n%s -> %s:%n", p.getLeft(), p.getRight());
                for (Clazz clazz : l.slice(c -> c.uses(r)).getClazzes()) {
                    String usages = clazz.getDependencies().slice(r).getClazzes().stream()
                            .map(name).collect(Collectors.joining(", "));
                    System.out.printf("  %s uses %s%n", name.apply(clazz), usages);
                }
            }
        });
    }

    @Test
    void checkJUnit5IsCycleFree() {
        Slice junit5 = cp.slice("org.junit..*")
                .minus("..shadow..*") // shadow packages don't belong to junit itself
                .minus(this::isDeprecated); // ignore deprecated classes
        dessert(junit5.partitionByPackage()).isCycleFree();
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

    private boolean isDeprecatedUsingReflection(Clazz clazz) {
        try {
            return clazz.getClassImpl().getAnnotation(Deprecated.class) != null;
        } catch (NoClassDefFoundError er) {
            // ignore some Kotlin classes that can't be loaded through reflection
            return false;
        }
    }
}
