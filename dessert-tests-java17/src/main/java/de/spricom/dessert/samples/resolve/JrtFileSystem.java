package de.spricom.dessert.samples.resolve;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete implementation of ReflectivJrtFileSystem.
 */
public class JrtFileSystem {

    private FileSystem jrtFileSystem;

    JrtFileSystem() throws IOException {
        jrtFileSystem = FileSystems.newFileSystem(URI.create("jrt:/"), Collections.emptyMap());
    }

    Path getModulePath(String moduleName) {
        return jrtFileSystem.getPath("/modules/" + moduleName, new String[0]);
    }

    boolean isDirectory(Path path) {
        return Files.isDirectory(path, new LinkOption[0]);
    }

    DirectoryStream<Path> newDirectoryStream(Path path) throws IOException {
        return Files.newDirectoryStream((Path)path);
    }

    URI toUri(Path path) throws URISyntaxException {
        URI uri = path.toUri();
        if (uri.toASCIIString().startsWith("jrt:/modules/")) {
            uri = new URI(uri.toASCIIString().replace("jrt:/modules/", "jrt:/"));
        }
        return uri;
    }

    String getFileName(Path path) {
        return ((Path)path).getFileName().toString();
    }

    List<String> listModules() throws IOException, URISyntaxException {
        List<String> moduleNames = new ArrayList<>(64);
        Path modulesRoot = jrtFileSystem.getPath("/modules", new String[0]);
        for (Path module : newDirectoryStream(modulesRoot)) {
            String path = toUri(module).getPath();
            moduleNames.add(path.substring(path.lastIndexOf('/') + 1));
        }
        return moduleNames;
    }

}
