package org.dessertj.jdeps;

import org.dessertj.util.ClassUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

public class JdepsWrapperTest {

    private JdepsWrapper wrapper;

    @BeforeEach
    void init() {
        wrapper = new JdepsWrapper();
    }

    @Test
    void testGetJdepsVersion() throws IOException, InterruptedException {
        String version = wrapper.getJdepsVersion();
        System.out.println("jdeps-version: " + version);
        assertThat(version).isNotEmpty();
    }

    @Test
    void testAnalyze() throws IOException, InterruptedException {
        wrapper.addOptions("--multi-release", "21");
        wrapper.addOptions("--ignore-missing-deps");
        wrapper.addOptions("--no-recursive");
        wrapper.addOptions("--compile-time");
        wrapper.setClassPathOption("--module-path");
        JdepsResult result = wrapper.analyze(getClassesDirectory());
        Set<String> resultDependencies = result.getDependencies(JdepsResult.class.getName());

        assertThat(result.getClasses()).contains(JdepsWrapper.class.getName(), JdepsResult.class.getName());
        assertThat(resultDependencies).contains(TreeSet.class.getName());
    }

    private File getClassesDirectory() {
        return ClassUtils.getRootFile(JdepsWrapper.class);
    }
}
