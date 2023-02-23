package de.spricom.dessert.samples.modules.libb.magic;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class MagicServiceLookup {
    ServiceLoader<MagicService> loader = ServiceLoader
            .load(MagicService.class);

    public Iterator<MagicService> services(boolean refresh) {
        if (refresh) {
            loader.reload();
        }
        return loader.iterator();
    }}
