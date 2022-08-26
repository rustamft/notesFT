package com.rustamft.notesft.domain.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class DateTimeStringBuilder {

    public static String millisToString(long milliseconds) {
        Instant instant = Instant.ofEpochMilli(milliseconds);
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern(Constants.DATE_TIME_PATTERN);
        return dateTime.format(dateTimeFormatter);
    }
}
