package org.dessertj.samples.resolve;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class Java11JrtFileSystemTest {

    private JrtFileSystem fs;
    private final Set<URI> uris = new HashSet<>();

    @BeforeEach
    void init() throws IOException {
        fs = new JrtFileSystem();
    }

    @Test
    void testResolvingJavaRuntimeModules() throws URISyntaxException, IOException {
        for (String module : fs.listModules()) {
            scan(fs.getModulePath(module), "");
        }
        assertThat(uris).contains(getUri(String.class));
    }

    private URI getUri(Class<?> clazz) throws URISyntaxException {
        String resource = "/" + clazz.getName().replace('.', '/') + ".class";
        URI uri = clazz.getResource(resource).toURI();
        return uri;
    }

    private void scan(Path dirPath, String prefix) throws IOException, URISyntaxException {
        for (Path path : fs.newDirectoryStream(dirPath)) {
            String filename = fs.getFileName(path);
            if (fs.isDirectory(path)) {
                String packageName = prefix + filename;
                scan(path, packageName + ".");
            } else if (filename.endsWith(".class")) {
                URI uri = fs.toUri(path);
                uris.add(uri);
            }
        }
    }
}