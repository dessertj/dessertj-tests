package de.spricom.dessert.samples.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class JrtFileSystemTest {

    @Test
    public void test() throws IOException {
        FileSystem jrt = FileSystems.newFileSystem(URI.create("jrt:/"), Collections.emptyMap());
        for (Path rootDirectory : jrt.getRootDirectories()) {
            System.out.println(rootDirectory);
            assertThat(Files.isDirectory(rootDirectory)).isTrue();
            iterate(rootDirectory);
        }
    }

    private void iterate(Path dir) throws IOException {
        DirectoryStream<Path> paths = Files.newDirectoryStream(dir);
        for (Path path : paths) {
            if (Files.isDirectory(path)) {
                System.out.println(path);
                iterate(path);
            }
        }
    }
}
