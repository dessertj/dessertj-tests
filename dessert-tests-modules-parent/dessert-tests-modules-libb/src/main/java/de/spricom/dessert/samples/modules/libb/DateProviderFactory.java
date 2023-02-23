package de.spricom.dessert.samples.modules.libb;

import de.spricom.dessert.samples.modules.libb.internal.DataProviderImpl;

public final class DateProviderFactory {

    private DateProviderFactory() {
    }

    public static DateProvider getDateProvider() {
        return new DataProviderImpl();
    }
}
