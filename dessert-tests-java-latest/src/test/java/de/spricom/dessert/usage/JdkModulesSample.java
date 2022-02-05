package de.spricom.dessert.usage;

import de.spricom.dessert.modules.ModuleRegistry;
import de.spricom.dessert.modules.core.ModuleSlice;
import de.spricom.dessert.modules.fixed.JavaModules;

public class JdkModulesSample {

    private ModuleRegistry registry = new ModuleRegistry();
    private JavaModules java = new JavaModules(registry);


    void test() {
        ModuleSlice management = java.management;
        ModuleSlice rmi = java.management.rmi;
    }
}
