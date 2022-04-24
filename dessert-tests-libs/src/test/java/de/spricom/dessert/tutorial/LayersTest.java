package de.spricom.dessert.tutorial;

import de.spricom.dessert.classfile.attribute.AttributeInfo;
import de.spricom.dessert.slicing.*;
import de.spricom.dessert.util.CombinationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

public class LayersTest {
    private static final Classpath cp = new Classpath();

    @Test
    void investigateJunitJupiterApi() {
        Root jupiter = cp.rootOf(Test.class);
        SortedMap<String, PackageSlice> packages = jupiter.partitionByPackage();
        CombinationUtils.combinations(new ArrayList<>(packages.keySet())).forEach(p -> {
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
        Root jupiter = cp.rootOf(JupiterEngineExecutionContext.class);
        SortedMap<String, PackageSlice> packages = jupiter.partitionByPackage();
        CombinationUtils.combinations(new ArrayList<>(packages.keySet())).forEach(p -> {
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
        Slice junit5 = cp.slice("org.junit..*")
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
}
