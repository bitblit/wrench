package com.erigir.wrench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Just some simple common date functions I use
 *
 * cweiss : 1/23/12 6:08 PM
 */
public class DateUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DateUtils.class);

    public static Date nDaysAgo(int days) {
        Calendar ago = Calendar.getInstance();
        ago.add(Calendar.DAY_OF_YEAR, -1 * days);
        return ago.getTime();
    }

    public static Date yearAgo() {
        return nDaysAgo(365);
    }

    public static Date monthAgo() {
        return nDaysAgo(31);
    }

}
