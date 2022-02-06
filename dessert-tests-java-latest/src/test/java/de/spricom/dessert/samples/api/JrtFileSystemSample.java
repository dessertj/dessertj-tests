package de.spricom.dessert.samples.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JrtFileSystemSample {

    @Test
    public void dump() throws IOException {
        FileSystem jrt = FileSystems.newFileSystem(URI.create("jrt:/"), Collections.emptyMap());
        for (Path rootDirectory : jrt.getRootDirectories()) {
            System.out.println(rootDirectory);
            assertThat(Files.isDirectory(rootDirectory)).isTrue();
            iterate(rootDirectory);
        }
    }

    @Test
    public void dumpModules() throws IOException {
        FileSystem jrt = FileSystems.newFileSystem(URI.create("jrt:/"), Collections.emptyMap());
        Path modules = jrt.getPath("/modules");
        iterate(modules);
    }

    private void iterate(Path dir) throws IOException {
        DirectoryStream<Path> paths = Files.newDirectoryStream(dir);
        for (Path path : paths) {
            if (Files.isDirectory(path)) {
                iterate(path);
            } else if (path.endsWith("module-info.class")) {
                System.out.println(path);
                assertThat(Files.isReadable(path)).isTrue();
                assertThat(Files.isRegularFile(path)).isTrue();
            }
        }
    }

    @Test
    public void withoutNIO() throws IOException {
        URI uri = URI.create("jrt://modules/java.base/module-info.class");
        Object content = uri.toURL().getContent();
        System.out.println("content:\n" + content);
    }

    @Test
    public void testlistModuleContent() throws IOException {
        listModuleContent("java.base").forEach(System.out::println);
    }

    public List<URI> listModuleContent(String moduleName) throws IOException {
        FileSystem jrt = FileSystems.newFileSystem(URI.create("jrt:/"), Collections.emptyMap());
        Path modulePath = jrt.getPath("/modules/" + moduleName);
        List<URI> content = new ArrayList<>(2048);
        traverse(modulePath, content);
        return content;
    }

    private void traverse(Path dir, List<URI> content) throws IOException {
        DirectoryStream<Path> paths = Files.newDirectoryStream(dir);
        for (Path path : paths) {
            if (Files.isDirectory(path)) {
                traverse(path, content);
            } else {
                content.add(path.toUri());
            }
        }
    }

    private ReflectiveFileSystem fs;

    @Test
    void testReflectiveFileSystem() throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        fs = new ReflectiveFileSystem();
        listModuleContentReflective("java.base").forEach(System.out::println);
    }

    public List<URI> listModuleContentReflective(String moduleName)
            throws InvocationTargetException, IllegalAccessException {
        Object modulePath = fs.getModulePath(moduleName);
        List<URI> content = new ArrayList<>(2048);
        traverseReflective(modulePath, content);
        return content;
    }

    private void traverseReflective(Object dirPath, List<URI> content)
            throws InvocationTargetException, IllegalAccessException {
        Iterable<Object> paths = fs.newDirectoryStream(dirPath);
        for (Object path : paths) {
            if (fs.isDirectory(path)) {
                traverseReflective(path, content);
            } else {
                content.add(fs.toUri(path));
            }
        }
    }

    static class ReflectiveFileSystem {
        private final Class<?> fileSystems;
        private final Method newFileSystem;

        private final Class<?> fileSystem;
        private final Method getPath;

        private final Class<?> path;
        private final Method toUri;

        private final Class<?> files;
        private final Method newDirectoryStream;
        private final Method isDirectory;

        private final Class<?> linkOption;
        private final Object emptyLinkOptionsArray;

        private final Object jrtFileSystem;

        ReflectiveFileSystem() throws ClassNotFoundException, NoSuchMethodException,
                InvocationTargetException, IllegalAccessException {
            fileSystems = Class.forName("java.nio.file.FileSystems");
            newFileSystem = fileSystems.getMethod("newFileSystem", URI.class, Map.class);

            fileSystem = Class.forName("java.nio.file.FileSystem");
            getPath = fileSystem.getMethod("getPath", String.class, String[].class);

            path = Class.forName("java.nio.file.Path");
            toUri = path.getMethod("toUri");

            files = Class.forName("java.nio.file.Files");
            newDirectoryStream = files.getMethod("newDirectoryStream", path);

            linkOption = Class.forName("java.nio.file.LinkOption");
            emptyLinkOptionsArray = Array.newInstance(linkOption, 0);
            isDirectory = files.getMethod("isDirectory", path, emptyLinkOptionsArray.getClass());

            jrtFileSystem = newFileSystem.invoke(null, URI.create("jrt:/"), Collections.emptyMap());
        }

        Object getModulePath(String moduleName) throws InvocationTargetException, IllegalAccessException {
            return getPath.invoke(jrtFileSystem, "/modules/" + moduleName, new String[0]);
        }

        boolean isDirectory(Object path) throws InvocationTargetException, IllegalAccessException {
            return ((Boolean) isDirectory.invoke(null, path, emptyLinkOptionsArray)).booleanValue();
        }

        Iterable<Object> newDirectoryStream(Object path) throws InvocationTargetException, IllegalAccessException {
            return (Iterable<Object>) newDirectoryStream.invoke(null, path);
        }

        URI toUri(Object path) throws InvocationTargetException, IllegalAccessException {
            return (URI) toUri.invoke(path);
        }
    }
}
