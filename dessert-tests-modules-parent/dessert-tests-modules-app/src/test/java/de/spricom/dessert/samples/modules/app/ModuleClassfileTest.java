package de.spricom.dessert.samples.modules.app;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.samples.modules.lib.Greeter;
import de.spricom.dessert.samples.modules.libb.magic.MagicService;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ModuleClassfileTest {

    @Test
    public void dumpAppModule() throws IOException {
        ClassFile cf = new ClassFile(App.class.getResourceAsStream("/module-info.class"));
        System.out.println(cf.dump());
    }

    @Test
    public void dumpLibModule() throws IOException {
        ClassFile cf = new ClassFile(Greeter.class.getResourceAsStream("/module-info.class"));
        System.out.println(cf.dump());
    }

    @Test
    public void dumpLib2Module() throws IOException {
        ClassFile cf = new ClassFile(MagicService.class.getResourceAsStream("/module-info.class"));
        System.out.println(cf.dump());
    }
}