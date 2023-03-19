package org.dessertj.samples.modules.app;

import org.dessertj.classfile.ClassFile;
import org.dessertj.samples.modules.lib.Greeter;
import org.dessertj.samples.modules.libb.magic.MagicService;
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