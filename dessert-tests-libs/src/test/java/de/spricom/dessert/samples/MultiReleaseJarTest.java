package de.spricom.dessert.samples;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import de.spricom.dessert.slicing.Root;
import org.apache.logging.log4j.util.Base64Util;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiReleaseJarTest {

    private final Classpath cp = new Classpath();

    @Test
    void testLog4j() {
        Clazz clazz = cp.asClazz(Base64Util.class);
        Root log4j = clazz.getRoot();

        assertThat(clazz.getAlternatives()).hasSize(2);
        assertThat(clazz.getAlternatives().stream().map(Clazz::getMinVersion)).containsOnly(null, "9");
        assertThat(log4j.slice(Base64Util.class.getName()).getClazzes()).hasSize(2);
        assertThat(cp.slice(Base64Util.class.getName()).getClazzes()).hasSize(2);
    }

}
