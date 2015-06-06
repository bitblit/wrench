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
 * A couple of simplistic functions for doing HTTP connections without a full HTTPclient dependency
 *
 * cweiss : 1/23/12 6:08 PM
 */
public class SimpleHttpUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpUtils.class);

    public static byte[] quietFetchUrl(String urlString, int timeoutInMS, int retries) {
        int readTimeout = 25000;
        int retryCount = 0;
        byte[] rval = null;

        while (rval == null && retryCount < retries) {
            try {
                retryCount++;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(2500); // 2.5 seconds to connect
                connection.setReadTimeout(readTimeout);
                connection.addRequestProperty("Accept-Encoding", "gzip");
                connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:20.0) Gecko/20100101 Firefox/20.0");

                rval = ZipUtils.toByteArray(connection.getInputStream());

                if ("gzip".equals(connection.getHeaderField("Content-Encoding"))) {
                    int pre = rval.length;
                    rval = ZipUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(rval)));
                    LOG.debug("Decomp {} to {}", pre, rval.length);
                }

            } catch (Exception e) {
                LOG.info("Failed reading {} - try {} of {}", new Object[]{urlString, retryCount, retries});

            }
        }
        if (rval == null) {
            throw new IllegalStateException("After " + retries + " tries, was unable to fetch " + urlString + " : Giving up");
        }

        return rval;
    }

    public static String quietFetchUrlAsString(String urlString, int timeout, int retries) {
        return new String(quietFetchUrl(urlString, timeout, retries));
    }

}
