package org.dessertj.samples.modules.app;

import org.dessertj.samples.modules.lib.Greeter;
import org.dessertj.samples.modules.libb.magic.MagicService;
import org.dessertj.samples.modules.libb.magic.MagicServiceLookup;

public class App {

    public static void main(String[] args) {
        System.out.println(Greeter.greet("World"));

        MagicServiceLookup lookup = new MagicServiceLookup();
        MagicService service = lookup.services(true).next();
        System.out.printf("The magic word is %s.%n", service.magicWord());
    }
}
