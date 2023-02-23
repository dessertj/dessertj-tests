import de.spricom.dessert.samples.modules.libb.magic.MagicService;
import de.spricom.dessert.samples.modules.libb.magic.abra.AbraService;
import de.spricom.dessert.samples.modules.libb.magic.buz.BuzService;

module dessert.tests.modules.libb {
    exports de.spricom.dessert.samples.modules.libb.magic;
    exports de.spricom.dessert.samples.modules.libb to dessert.tests.modules.lib;

    uses MagicService;
    provides MagicService with AbraService, BuzService;
}