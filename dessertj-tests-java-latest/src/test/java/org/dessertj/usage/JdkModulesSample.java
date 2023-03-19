package org.dessertj.usage;

import org.dessertj.modules.ModuleRegistry;
import org.dessertj.modules.core.ModuleSlice;
import org.dessertj.modules.fixed.JavaModules;

public class JdkModulesSample {

    private ModuleRegistry registry = new ModuleRegistry();
    private JavaModules java = new JavaModules(registry);


    void test() {
        ModuleSlice management = java.management;
        ModuleSlice rmi = java.management.rmi;
    }
}
