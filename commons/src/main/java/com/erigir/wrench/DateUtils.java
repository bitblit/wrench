package com.erigir.wrench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

/**
 * Just some simple common date functions I use
 * <p>
 * cweiss : 1/23/12 6:08 PM
 */
public class DateUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DateUtils.class);

    /**
     * Returns a date object that is N days ago (relative to now (ie, new Date())
     */
    public static Date nDaysAgo(int days) {
        Calendar ago = Calendar.getInstance();
        ago.add(Calendar.DAY_OF_YEAR, -1 * days);
        return ago.getTime();
    }

    /**
     * Returns a date object that is N years ago (relative to now (ie, new Date())
     */
    public static Date yearsAgo(int number) {
        Calendar ago = Calendar.getInstance();
        ago.add(Calendar.YEAR, -1 * number);
        return ago.getTime();

    }

    public static Date monthsAgo(int months) {
        Calendar ago = Calendar.getInstance();
        ago.add(Calendar.MONTH, -1 * months);
        return ago.getTime();

    }

}
