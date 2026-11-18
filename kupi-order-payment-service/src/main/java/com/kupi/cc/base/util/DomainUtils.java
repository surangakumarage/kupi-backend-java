package com.kupi.cc.base.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

public class DomainUtils {

    private DomainUtils() {
        /*
         * private constructor is used to hide implicit default constructor
         */
    }

    public static boolean isValidExpiryDate(long expiryDate) {
        return new Date().getTime() < expiryDate;
    }

    public static Date setTimeOfDate(Date date, int hourOfDay, int minute, int second) {
        return setTimeOfDate(date.getTime(), hourOfDay, minute, second);
    }

    public static Date setTimeOfDate(long dateTime, int hourOfDay, int minute, int second) {
        var calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return calendar.getTime();
    }

    public static boolean isSameDate(long time1, long time2) {
        LocalDate localDate1 = new Date(time1).toInstant()
                .atOffset( ZoneOffset.UTC ).toLocalDate();
        LocalDate localDate2 = new Date(time2).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate1.isEqual(localDate2);
    }


}
