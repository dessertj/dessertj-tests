package de.spricom.dessert.samples.modules.lib;

import de.spricom.dessert.samples.modules.lib2.DateProvider;
import de.spricom.dessert.samples.modules.lib2.DateProviderFactory;

public final class Greeter {

    public static String greet(String name) {
        DateProvider provider = DateProviderFactory.getDateProvider();
        return String.format("Hello %s, it's %s.", name, provider.currentDate());
    }
}
