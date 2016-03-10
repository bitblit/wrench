package com.erigir.wrench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

/**
 * A couple of simplistic functions for doing HTTP connections without a full HTTPclient dependency
 * <p>
 * cweiss : 1/23/12 6:08 PM
 */
public class SimpleHttpUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpUtils.class);

    public static byte[] quietFetchUrl(String urlString, int timeoutInMS, int retries) {
        HttpTx tx = quietFetchUrlDetails(urlString, timeoutInMS, retries);
        return (tx==null)?null:tx.bodyContents;
    }

    public static HttpTx postDataToURL(String sUrl,  Map<String,String> headers, byte[] data) {
        HttpTx rval = null;
        try {
            LOG.info("Sending {} bytes to {}, hash={}", new Object[]{data.length, sUrl});

            URL u = new URL(sUrl);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setConnectTimeout(2500); // 2.5 seconds to connect
            connection.setReadTimeout(25000); // 25 seconds to read
            connection.addRequestProperty("Accept-Encoding", "gzip");

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            for (Map.Entry<String,String> e:headers.entrySet())
            {
                connection.setRequestProperty(e.getKey(), e.getValue());
            }
            connection.setRequestProperty("Content-Length", "" + data.length);
            connection.setUseCaches(false);

            connection.getOutputStream().write(data);
            connection.getOutputStream().flush();

            byte[] bodyData = ZipUtils.toByteArray(connection.getInputStream());
            if ("gzip".equals(connection.getHeaderField("Content-Encoding"))) {
                int pre = bodyData.length;
                bodyData = ZipUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(bodyData)));
                LOG.trace("Decomp {} to {}", pre, bodyData.length);
            }
            rval = new HttpTx();
            rval.setBodyContents(bodyData);
            rval.setHeaders(convertHeaders(connection.getHeaderFields()));
            rval.setStatus(connection.getResponseCode());

        } catch (Exception e) {
            LOG.error("Error during post", e);
            rval = null;
        }
        return rval;
    }

    /**
     * Attempts up to 'retries' times to read the contents of a URL (times out in timeoutMS if
     * cannot read) - throws an IllegalStateException if it cannot successfully read
     * NOTE: This function properly handles Content-Encoding: GZIP
     * @param urlString String containing the URL to fetch
     * @param timeoutInMS int containing the # of ms to wait for the url
     * @param retries int containing the number of times to retry reading the url
     * @return byte[] containing the body from the HTTP request
     */
    public static HttpTx quietFetchUrlDetails(String urlString, int timeoutInMS, int retries) {
        int readTimeout = 25000;
        int retryCount = 0;
        HttpTx rval = null;

        while (rval == null && retryCount < retries) {
            try {
                retryCount++;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(2500); // 2.5 seconds to connect
                connection.setReadTimeout(readTimeout);
                connection.addRequestProperty("Accept-Encoding", "gzip");
                connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:20.0) Gecko/20100101 Firefox/20.0");

                byte[] bodyData = ZipUtils.toByteArray(connection.getInputStream());


                if ("gzip".equals(connection.getHeaderField("Content-Encoding"))) {
                    int pre = bodyData.length;
                    bodyData = ZipUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(bodyData)));
                    LOG.debug("Decomp {} to {}", pre, bodyData.length);
                }
                rval = new HttpTx();
                rval.setBodyContents(bodyData);
                rval.setHeaders(convertHeaders(connection.getHeaderFields()));
                rval.setStatus(connection.getResponseCode());
            } catch (Exception e) {
                LOG.info("Failed reading {} - try {} of {}", new Object[]{urlString, retryCount, retries});

            }
        }
        if (rval == null) {
            throw new IllegalStateException("After " + retries + " tries, was unable to fetch " + urlString + " : Giving up");
        }

        return rval;
    }

    private static Map<String,String> convertHeaders(Map<String,List<String>> headersIn)
    {
        Map<String,String> rval = new TreeMap<>();
        for (Map.Entry<String,List<String>> e:headersIn.entrySet())
        {
            if (e.getKey()!=null && e.getValue()!=null && e.getValue().size()>0)
            {
                rval.put(e.getKey(), e.getValue().get(0));
            }
        }
        return rval;
    }

    /*
     * Like the byte[] version, but wrapped in a String object
     */
    public static String quietFetchUrlAsString(String urlString, int timeout, int retries) {
        return new String(quietFetchUrl(urlString, timeout, retries));
    }

    public static class HttpTx
    {
        private byte[] bodyContents;
        private Map<String,String> headers = new TreeMap<>();
        private int status;

        public byte[] getBodyContents() {
            return bodyContents;
        }

        public void setBodyContents(byte[] bodyContents) {
            this.bodyContents = bodyContents;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }


    }
}
