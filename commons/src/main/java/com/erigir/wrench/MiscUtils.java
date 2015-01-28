package com.erigir.wrench;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Terrible name, I know - what can you do?  Need to refactor
 */
public class MiscUtils {

    /**
     * Null-safe .equals method
     *
     * @param o1
     * @param o2
     * @return
     */
    public static boolean nse(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        return !(o1 == null || o2 == null) && o1.equals(o2);
    }

    /**
     * Null-safe add for big decimal
     * @param b1
     * @param b2
     * @return
     */
    public static BigDecimal nsa(BigDecimal b1, BigDecimal b2) {
        BigDecimal ib1 = (b1 == null) ? BigDecimal.ZERO : b1;
        BigDecimal ib2 = (b2 == null) ? BigDecimal.ZERO : b2;
        return ib1.add(ib2);
    }

    public static String throwableToString(String prefix,Throwable t) {
        StringBuilder rval = new StringBuilder();
        if (prefix!=null)
        {
            rval.append(prefix);
        }
        rval.append(t.toString());
        rval.append("\n");
        for (StackTraceElement ste : t.getStackTrace()) {
            rval.append(ste);
            rval.append("\n");
        }
        return rval.toString();
    }

    public static int calculateDifference(Date a, Date b) {
        int tempDifference = 0;
        int difference = 0;
        Calendar earlier = Calendar.getInstance();
        Calendar later = Calendar.getInstance();

        if (a.compareTo(b) < 0) {
            earlier.setTime(a);
            later.setTime(b);
        } else {
            earlier.setTime(b);
            later.setTime(a);
        }

        while (earlier.get(Calendar.YEAR) != later.get(Calendar.YEAR)) {
            tempDifference = 365 * (later.get(Calendar.YEAR) - earlier.get(Calendar.YEAR));
            difference += tempDifference;

            earlier.add(Calendar.DAY_OF_YEAR, tempDifference);
        }

        if (earlier.get(Calendar.DAY_OF_YEAR) != later.get(Calendar.DAY_OF_YEAR)) {
            tempDifference = later.get(Calendar.DAY_OF_YEAR) - earlier.get(Calendar.DAY_OF_YEAR);
            difference += tempDifference;

            earlier.add(Calendar.DAY_OF_YEAR, tempDifference);
        }

        return difference;
    }

    /**
     * This is here because SimpleDateFormat isn't null safe
     * @param date
     * @param format
     * @return
     */
    public static String simpleFormat(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String safeEncode(String in) {
        try {
            return (in == null) ? null : URLEncoder.encode(in, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Cant happen - utf-8 is valid");
        }
    }

    public static String toMemoryFormat(long amount) {
        BigDecimal div = new BigDecimal(1024);
        String suf = "bytes";
        BigDecimal v = new BigDecimal((float) amount);
        if (v.longValue() > 1024) {
            suf = "K";
            v = v.divide(div, RoundingMode.HALF_EVEN);
        }
        if (v.longValue() > 1024) {
            suf = "M";
            v = v.divide(div, RoundingMode.HALF_EVEN);
        }
        if (v.longValue() > 1024) {
            suf = "G";
            v = v.divide(div, RoundingMode.HALF_EVEN);
        }

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        return nf.format(v) + " " + suf;
    }

    public static String defaultedGet(Properties props, String name, String def) {
        String rval = null;
        if (props != null) {
            rval = props.getProperty(name);
        }
        return (rval == null) ? def : rval;
    }

}
