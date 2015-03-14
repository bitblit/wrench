package com.erigir.wrench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utils for hiding exceptions that should never have been checked exceptions
 * Created by chrweiss on 3/13/15.
 */
public class QuietUtils {
    private static final Logger LOG = LoggerFactory.getLogger(QuietUtils.class);

    public static void quietSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            LOG.trace("Interrupted Exception caught", ie);
        }
    }

    public static Date quietParse(String dateString, DateFormat dateFormat) {
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException pe) {
            throw new IllegalArgumentException("Error occurred in parse", pe);
        }
    }

    public static Date quietParse(String dateString, String dateFormat) {
        try {
            // Slow, but these aren't threadsafe anyway
            return new SimpleDateFormat(dateFormat).parse(dateString);
        } catch (ParseException pe) {
            throw new IllegalArgumentException("Error occurred in parse", pe);
        }
    }
}
