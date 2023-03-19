import org.dessertj.samples.modules.libb.magic.MagicService;
import org.dessertj.samples.modules.libb.magic.abra.AbraService;
import org.dessertj.samples.modules.libb.magic.buz.BuzService;

module dessert.tests.modules.libb {
    exports org.dessertj.samples.modules.libb.magic;
    exports org.dessertj.samples.modules.libb to dessert.tests.modules.lib;

    uses MagicService;
    provides MagicService with AbraService, BuzService;
}