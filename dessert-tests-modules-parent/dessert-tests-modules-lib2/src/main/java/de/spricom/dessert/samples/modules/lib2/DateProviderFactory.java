package de.spricom.dessert.samples.modules.lib2;

import de.spricom.dessert.samples.modules.lib2.internal.DataProviderImpl;

public final class DateProviderFactory {

    private DateProviderFactory() {
    }

    public static DateProvider getDateProvider() {
        return new DataProviderImpl();
    }
}
