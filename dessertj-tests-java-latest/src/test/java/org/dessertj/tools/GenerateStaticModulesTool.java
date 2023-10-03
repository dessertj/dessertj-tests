package org.dessertj.tools;

import org.dessertj.classfile.ClassFile;
import org.dessertj.classfile.attribute.Attributes;
import org.dessertj.classfile.attribute.ModuleAttribute;
import org.dessertj.classfile.attribute.ModulePackagesAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GenerateStaticModulesTool {
    private Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    // -Dtarget.src.dir=/home/hjhessmann/code/dessertj/dessertj-core/src/main/java
    private static final String SRC = System.getProperty("target.src.dir", "target/generated-sources");
    private static final Path DIR = Path.of(SRC, "org.dessertj.modules".split("\\."));

    private PrintWriter out;
    private FileSystem jrt;
    private final SortedMap<String, ModuleDef> modules = new TreeMap<>();
    private final SortedMap<String, Node> moduleNames = new TreeMap<>();
    private final SortedMap<String, Node> packageNames = new TreeMap<>();
    private final String generatedBy = "/**\n" +
            " * Generated by " + this.getClass().getName()+ ".\n" +
            " */";

    @BeforeEach
    void init() throws IOException {
        jrt = FileSystems.newFileSystem(URI.create("jrt:/"), Collections.emptyMap());
        scanModules();
        scanPackages();
    }

    @Test
    void generateFixedModules() throws IOException {
        createModules();
    }

    private void createModules() throws IOException {
        for (ModuleDef module : modules.values()) {
            createModule(module);
        }
    }

    private void createModule(ModuleDef moduleDef) throws IOException {
        Path dir = DIR.resolve(moduleDef.dirName());
        Files.createDirectories(dir);
        File file = new File(dir.toFile(), moduleDef.fileName() + "Module.java");
        try (OutputStreamWriter w = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(renderModule(moduleDef));
        }
    }

    private String renderModule(ModuleDef moduleDef) {
        return """
                package org.dessertj.modules.%1$s;
                                        
                import org.dessertj.modules.core.FixedModule;
                import org.dessertj.slicing.Classpath;
                import org.dessertj.slicing.Slices;
                                        
                %2$s
                class %3$sModule extends FixedModule {
                                        
                    %3$sModule(Classpath cp) {
                        super("%4$s", "%5$s",
                                Slices.of(
                                        %6$s
                                ),
                                Slices.of(
                                        %7$s
                                ));
                    }
                }
                """.formatted(
                moduleDef.dirName(),
                generatedBy,
                moduleDef.fileName(),
                moduleDef.name,
                moduleDef.version,
                renderSlice(moduleDef.exported),
                renderSlice(moduleDef.packages)
        );
    }

    private String renderSlice(SortedSet<String> packages) {
        return packages.stream()
                .map(s -> "cp.slice(\"" + s + ".*\")")
                .collect(Collectors.joining(",\n                        "));
    }

    @Test
    void generateModuleResolvers() throws IOException {
        for (String prefix : "java, jdk".split(", ")) {
            generateModuleResolver(prefix);
        }
    }

    private void generateModuleResolver(String prefix) throws IOException {
        Path dir = DIR.resolve(prefix);
        Files.createDirectories(dir);
        File file = new File(dir.toFile(), capitalize(prefix) + "ModulesResolver.java");
        try (OutputStreamWriter w = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(renderResolver(prefix));
        }
    }

    private String renderResolver(String prefix) {
        return """
                package org.dessertj.modules.%1$s;

                import org.dessertj.modules.core.FixedModule;
                import org.dessertj.modules.core.ModuleResolver;
                import org.dessertj.slicing.Classpath;

                import java.util.Arrays;
                import java.util.List;

                %4$s
                public class %2$sModulesResolver implements ModuleResolver {

                    private final List<FixedModule> modules;

                    public %2$sModulesResolver(Classpath cp) {
                        modules =
                                Arrays.asList(
                                        %3$s
                                );
                    }

                    @Override
                    public List<FixedModule> getModules() {
                        return modules;
                    }
                }
                """.formatted(
                prefix,
                capitalize(prefix),
                renderModuleList(prefix),
                generatedBy
        );
    }

    private String renderModuleList(String prefix) {
        return modules.values().stream()
                .filter(module -> module.dirName().equals(prefix))
                .map(ModuleDef::fileName)
                .map(name -> String.format("new %sModule(cp)", name))
                .collect(Collectors.joining(",\n                        "));
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }


    @Test
    void generateModulesConstants() throws IOException {
        Node root = moduleNames.get("");
        for (Node node : root.children.values()) {
            generateModulesConstants(node);
        }
    }

    private void generateModulesConstants(Node node) throws IOException {
        Path dir = DIR.resolve("fixed");
        Files.createDirectories(dir);
        File file = new File(dir.toFile(), capitalize(node.name) + "Modules.java");
        try (OutputStreamWriter w = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(renderModulesConstants(node));
        }
    }

    private String renderModulesConstants(Node node) {
        return """
                package org.dessertj.modules.fixed;

                import org.dessertj.modules.core.DelegateModule;
                import org.dessertj.modules.core.ModuleSlice;
                import org.dessertj.modules.core.ModuleLookup;

                %5$s
                public final class %1$sModules {

                %2$s
                %4$s
                    public %1$sModules(ModuleLookup registry) {
                        %3$s
                    }
                }
                """.formatted(
                capitalize(node.name),
                renderModuleVariables(node, 1),
                renderModuleAssignments(node, 1),
                renderSubConstants(node),
                generatedBy
        );
    }

    private String renderSubConstants(Node node) {
        StringBuilder sb = new StringBuilder(node.children.size() * 100);
        for (Node child : node.children.values()) {
            if (!child.children.isEmpty()) {
                if (modules.containsKey(child.fullName())) {
                    sb.append(renderSubModule(child));
                } else {
                    sb.append(renderSubConstant(child));
                }
            }
            sb.append(renderSubConstants(child));
        }
        return sb.toString();
    }

    private String renderSubConstant(Node node) {
        return """
                    public static final class %1$s {

                %2$s
                        %1$s(ModuleLookup registry) {
                            %3$s
                        }
                    }
                    
                """.formatted(
                camelCaseName(node),
                renderModuleVariables(node, 2),
                renderModuleAssignments(node, 2)
                );
    }

    private String renderSubModule(Node node) {
        return """
                    public static final class %1$s extends DelegateModule {

                %3$s
                        %1$s(ModuleLookup registry) {
                            super(registry.getModule("%2$s"));
                            %4$s
                        }
                    }
                    
                """.formatted(
                camelCaseName(node),
                node.fullName(),
                renderModuleVariables(node, 2),
                renderModuleAssignments(node, 2)
        );
    }

    private String renderModuleVariables(Node node, int indent) {
        StringBuilder sb = new StringBuilder(40 * node.children.size());
        for (Node child : node.children.values()) {
            sb.append("    ".repeat(indent))
                    .append("public final ")
                    .append(child.children.isEmpty() ? "ModuleSlice" : camelCaseName(child))
                    .append(" ")
                    .append(child.name)
                    .append(";\n");
        }
        return sb.toString();
    }

    private String renderModuleAssignments(Node node, int indent) {
        return node.children.values().stream()
                .map(this::renderModuleAssignment)
                .collect(Collectors.joining("\n    " + "    ".repeat(indent)));
    }

    private String renderModuleAssignment(Node node) {
        if (node.children.isEmpty()) {
            return "%s = registry.getModule(\"%s\");".formatted(node.name, node.fullName());
        } else {
            return "%s = new %s(registry);".formatted(node.name, camelCaseName(node));
        }
    }

    private String camelCaseName(Node node) {
        String[] parts = node.fullName().split("\\.");
        return Arrays.stream(parts).skip(1).map(this::capitalize).collect(Collectors.joining());
    }


    private void scanModules() throws IOException {
        Path modules = jrt.getPath("/modules");
        DirectoryStream<Path> paths = Files.newDirectoryStream(modules);
        for (Path path : paths) {
            if (Files.isDirectory(path)) {
                scanModule(path);
            }
        }
    }

    private void scanModule(Path modulePath) throws IOException {
        Path moduleInfoPath = modulePath.resolve("module-info.class");
        assert Files.isRegularFile(moduleInfoPath) : "There is no " + moduleInfoPath + " file!";
        try (InputStream is = Files.newInputStream(moduleInfoPath, StandardOpenOption.READ)) {
            ClassFile cf = new ClassFile(is);
            List<ModuleAttribute> moduleAttributes = Attributes.filter(cf.getAttributes(), ModuleAttribute.class);
            assert moduleAttributes.size() == 1 : "There are " + moduleAttributes.size() + " module attibutes in " + moduleInfoPath;
            ModuleAttribute moduleAttribute = moduleAttributes.get(0);

            ModuleDef module = new ModuleDef();
            module.name = moduleAttribute.getModuleName();
            module.version = moduleAttribute.getModuleVersion();

            for (ModuleAttribute.Export export : moduleAttribute.getExports()) {
                if (export.isUnqualified()) {
                    module.exported.add(export.getPackageName());
                }
            }

            List<ModulePackagesAttribute> modulePackagesAttributes = Attributes.filter(cf.getAttributes(), ModulePackagesAttribute.class);
            if (!modulePackagesAttributes.isEmpty()) {
                ModulePackagesAttribute modulePackagesAttribute = modulePackagesAttributes.get(0);
                module.packages.addAll(Arrays.asList(modulePackagesAttribute.getPackageNames()));
            }
            modules.put(module.name, module);
            addName(moduleNames, module.name);
        }
    }

    private void scanPackages() throws IOException {
        Path packages = jrt.getPath("/packages");
        DirectoryStream<Path> paths = Files.newDirectoryStream(packages);
        for (Path path : paths) {
            if (Files.isDirectory(path)) {
                String packageName = path.getFileName().toString();
                addName(packageNames, packageName);
            }
        }
    }

    private void addName(SortedMap<String, Node> names, String fullName) {
        String[] parts = fullName.split("\\.");
        String name = "";
        Node parent = names.computeIfAbsent(name, n -> new Node(null, n));
        for (String part : parts) {
            name += part;
            Node node = names.get(name);
            if (node == null) {
                node = new Node(parent, part);
                names.put(name, node);
            }
            parent = node;
            name += ".";
        }
    }

    static class ModuleDef {
        String name;
        String version;
        final SortedSet<String> exported = new TreeSet<>();
        final SortedSet<String> packages = new TreeSet<>();

        String variableName() {
            return name.substring(name.indexOf(".") + 1);
        }

        String dirName() {
            return name.substring(0, name.indexOf("."));
        }

        String fileName() {
            String[] parts = name.split("\\.");
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
            }
            return sb.toString();
        }
    }

    static class Node {
        final Node parent;
        final String name;
        final SortedMap<String, Node> children = new TreeMap<>();

        Node(Node parent, String name) {
            this.parent = parent;
            this.name = name;
            if (parent != null) {
                parent.children.put(this.name, this);
            }
        }

        String fullName() {
            return parent == null || parent.parent == null ? name : parent.fullName() + "." + name;
        }
    }
}
