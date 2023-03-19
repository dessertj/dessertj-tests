package org.dessertj.samples.modules.libb;

import org.dessertj.samples.modules.libb.internal.DataProviderImpl;

public final class DateProviderFactory {

    private DateProviderFactory() {
    }

    public static DateProvider getDateProvider() {
        return new DataProviderImpl();
    }
}
