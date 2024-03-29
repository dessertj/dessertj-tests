package org.dessertj.samples.modules.libb.internal;

import org.dessertj.samples.modules.libb.DateProvider;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DataProviderImpl implements DateProvider {

    @Override
    public String currentDate() {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ZonedDateTime.now());
    }
}
