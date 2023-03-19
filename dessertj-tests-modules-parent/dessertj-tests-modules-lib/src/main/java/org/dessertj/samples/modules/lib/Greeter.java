package org.dessertj.samples.modules.lib;

import org.dessertj.samples.modules.libb.DateProvider;
import org.dessertj.samples.modules.libb.DateProviderFactory;

public final class Greeter {

    public static String greet(String name) {
        DateProvider provider = DateProviderFactory.getDateProvider();
        return String.format("Hello %s, it's %s.", name, provider.currentDate());
    }
}
