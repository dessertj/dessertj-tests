import de.spricom.dessert.samples.modules.lib2.magic.MagicService;
import de.spricom.dessert.samples.modules.lib2.magic.abra.AbraService;
import de.spricom.dessert.samples.modules.lib2.magic.buz.BuzService;

module dessert.tests.modules.lib2 {
    exports de.spricom.dessert.samples.modules.lib2.magic;
    exports de.spricom.dessert.samples.modules.lib2 to dessert.tests.modules.lib;

    uses MagicService;
    provides MagicService with AbraService, BuzService;
}