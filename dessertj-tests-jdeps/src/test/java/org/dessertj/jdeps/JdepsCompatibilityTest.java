package org.dessertj.jdeps;

import org.dessertj.classfile.ClassFile;
import org.dessertj.classfile.FieldInfo;
import org.dessertj.classfile.MethodInfo;
import org.dessertj.classfile.attribute.*;
import org.dessertj.traversal.ClassVisitor;
import org.dessertj.traversal.PathProcessor;
import org.dessertj.util.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class JdepsCompatibilityTest implements ClassVisitor {
    private static final Logger log = LogManager.getLogger(JdepsCompatibilityTest.class);

    private final JdepsWrapper wrapper = new JdepsWrapper();
    private JdepsResult jdepsResult;

    private int rootCounter;
    private long classCounter;
    private long exactMatchesCounter;
    private final Map<String, Integer> additonalDependenciesCounters = new HashMap<>();

    @BeforeEach
    public void init() {
        wrapper.addOptions("--multi-release", "base");
        wrapper.setClassPathOption("--module-path");
    }

    @AfterEach
    public void showStatistics() {
        log.info(() -> "Additional dependencies found:\n" + dumpAddionalOccurrencies());

        log.info("Results:\n{}\n{}\n{}",
                String.format("%36s: %8d", "number of jar files or directories", rootCounter),
                String.format("%36s: %8d", "total number of classes", classCounter),
                String.format("%36s: %8d (%1.1f %%)", "number of exact matches",
                        exactMatchesCounter,
                        100.0 * exactMatchesCounter / classCounter));
    }

    private String dumpAddionalOccurrencies() {
        return additonalDependenciesCounters.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> e.getValue().longValue()))
                .map(e -> String.format("%6d times: %s", e.getValue(), e.getKey()))
                .collect(Collectors.joining("\n"));
    }

    @Test
    void testProjectClasses() throws IOException {
        PathProcessor proc = new PathProcessor() {
            @Override
            protected void processJar(File file, ClassVisitor visitor) {
            }

            @Override
            protected void processDirectory(File file, ClassVisitor visitor) throws IOException {
                analyze(file);
                super.processDirectory(file, visitor);
            }
        };
        check(proc);
    }

    @Test
    public void testJarsOnClassPath() throws IOException {
        PathProcessor proc = new PathProcessor() {
            @Override
            protected void processJar(File file, ClassVisitor visitor) throws IOException {
                if (!filterJar(file)) {
                    log.warn(() -> "Skipping " + file.getAbsolutePath());
                    return;
                }
                analyze(file);
                super.processJar(file, visitor);
            }

            @Override
            protected void processDirectory(File file, ClassVisitor visitor) {
            }
        };
        check(proc);
    }

    private boolean filterJar(File jarFile) {
        return skipNone(jarFile);
    }

    private boolean skipNone(File jarFile) {
        return true;
    }

    private boolean filterSingleJar(File jarFile) {
        return "spring-security-config-5.4.2.jar".equals(jarFile.getName());
    }

    private void analyze(File root) {
        rootCounter++;
        log.info("Analyzing {}", root);
        try {
            jdepsResult = wrapper.analyze(root);
        } catch (IOException ex) {
            throw new RuntimeException("Processing " + root + " failed.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void check(PathProcessor proc) throws IOException {
        proc.traverseAllClasses(this);
    }

    @Override
    public void visit(File root, String classname, InputStream content) {
        classCounter++;
        try {
            log.debug("Checking {}[{}]", classname, root.getName());
            ClassFile cf = new ClassFile(content);
            Set<String> cfdeps = cf.getDependentClasses();
            Set<String> jdeps = jdepsResult.getDependencies(classname);

            String name = classname + "[" + root.getName() + "]";
            assertThat(cfdeps).as(name).containsAll(jdeps);

            if (cfdeps.size() != jdeps.size()) {
                handleDiff(name, cf, cfdeps, jdeps);
            } else {
                exactMatchesCounter++;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Processing " + classname + " in " + root.getAbsolutePath() + " failed.", ex);
        }
    }

    private void handleDiff(String name, ClassFile cf, Set<String> cfdeps, Set<String> jdeps) {
        Set<String> diff = Sets.difference(cfdeps, jdeps);
        log.info(() -> "Dessert found additional dependencies for " + name + ":\n" + String.join("\n", diff));
        if (name.contains("module-info[")) {
            // jdeps ignores dependencies of module-info classes
            return;
        }
        countDiffs(diff);
        if (false) {
            Set<String> expectedDiff = determineDependenciesNotDetectedByJDeps(cf);
            expectedDiff.addAll(specialCases(name));
            assertThat(expectedDiff).containsAll(diff);
        }
    }

    private void countDiffs(Set<String> diff) {
        for (String name : diff) {
            additonalDependenciesCounters.merge(name, 1, Integer::sum);
        }
    }

    private Set<String> specialCases(String name) {
        return switch (name) {
            case "org.assertj.core.api.AbstractObjectAssert[assertj-core-3.18.1.jar]",
                    "org.assertj.core.api.InstanceOfAssertFactories[assertj-core-3.18.1.jar]" -> Set.of("org.assertj.core.api.Assert");
            default -> Collections.emptySet();
        };
    }

    private Set<String> determineDependenciesNotDetectedByJDeps(ClassFile cf) {
        Set<String> referencedClasses = new HashSet<>();
        referencedClasses.add(Object.class.getName()); // according to jdeps there are classes not depending on Object
        determineClassesReferencedByRuntimeAnnotations(referencedClasses, cf);
        determineClassesReferencedBySignatureAttribute(referencedClasses, cf);
        return referencedClasses;
    }

    /**
     * JDeps does not consider parameters of generic types to be a dependency.
     */
    private void determineClassesReferencedBySignatureAttribute(Set<String> referencedClasses, ClassFile cf) {
        for (AttributeInfo attribute : cf.getAttributes()) {
            if (attribute instanceof SignatureAttribute) {
                attribute.addDependentClassNames(referencedClasses);
            }
        }
    }

    /**
     * Jdeps does not consider parameters of a runtime-annotation to be a dependency.
     */
    private void determineClassesReferencedByRuntimeAnnotations(Set<String> referencedClasses, ClassFile cf) {
        collectReferencedClasses(referencedClasses, cf.getAttributes());
        for (FieldInfo fieldInfo : cf.getFields()) {
            collectReferencedClasses(referencedClasses, fieldInfo.getAttributes());
        }
        for (MethodInfo methodInfo : cf.getMethods()) {
            collectReferencedClasses(referencedClasses, methodInfo.getAttributes());
        }
    }

    private void collectReferencedClasses(Set<String> referencedClasses, AttributeInfo[] attributes) {
        for (AttributeInfo attribute : attributes) {
            if (attribute instanceof RuntimeVisibleAnnotationsAttribute) {
                collectReferencedClasses(referencedClasses, ((RuntimeVisibleAnnotationsAttribute) attribute).getAnnotations());
            }
            if (attribute instanceof RuntimeVisibleParameterAnnotationsAttribute) {
                for (ParameterAnnotation parameterAnnotation : ((RuntimeVisibleParameterAnnotationsAttribute) attribute).getParameterAnnotations()) {
                    collectReferencedClasses(referencedClasses, parameterAnnotation.getAnnotations());
                }
            }
        }
    }

    private void collectReferencedClasses(Set<String> referencedClasses, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            for (ElementValuePair elementValuePair : annotation.getElementValuePairs()) {
                elementValuePair.addDependentClassNames(referencedClasses);
            }
        }
    }
}
