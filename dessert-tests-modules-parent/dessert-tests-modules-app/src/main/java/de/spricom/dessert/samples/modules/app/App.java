package de.spricom.dessert.samples.modules.app;

import de.spricom.dessert.samples.modules.lib.Greeter;
import de.spricom.dessert.samples.modules.libb.magic.MagicService;
import de.spricom.dessert.samples.modules.libb.magic.MagicServiceLookup;

public class App {

    public static void main(String[] args) {
        System.out.println(Greeter.greet("World"));

        MagicServiceLookup lookup = new MagicServiceLookup();
        MagicService service = lookup.services(true).next();
        System.out.printf("The magic word is %s.%n", service.magicWord());
    }
}
