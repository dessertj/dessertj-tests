package de.spricom.dessert.samples.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;

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
}
