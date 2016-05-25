package com.erigir.wrench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

/**
 * A couple of simplistic functions for doing HTTP connections without a full HTTPClient dependency
 * <p>
 * cweiss : 1/23/12 6:08 PM
 */
public class SimpleHttpUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpUtils.class);

    /*
     * Yes, yes, I know.  Using this in a multithreaded environment is stupid.  Guess what, not everything is a
     * multithreaded environment, and using this in a single threaded environment is a much simpler interface.
     */
    public static HttpTx latestError() {
        // This copy is basically here to keep backwards compatibility.  Should probably toss it next
        // major release
        SimpleStreamHttpUtils.StreamHttpTx l = SimpleStreamHttpUtils.latestError();
        return (HttpTx)SimpleStreamHttpUtils.latestError().copyTo(new HttpTx());
    }

    public static byte[] quietFetchUrl(String urlString, int timeoutInMS, int retries) {
        HttpTx tx = quietFetchUrlDetails(urlString, timeoutInMS, retries);
        return (tx == null) ? null : tx.bodyContents;
    }

    public static HttpTx postDataToURL(String sUrl, Map<String, String> headers, byte[] data) {
        return httpRequestWithBody(sUrl, "POST", headers, data);
    }

    public static HttpTx putDataToURL(String sUrl, Map<String, String> headers, byte[] data) {
        return httpRequestWithBody(sUrl, "PUT", headers, data);
    }

    public static HttpTx sendDeleteToURL(String sUrl, Map<String, String> headers, byte[] data) {
        return httpRequestWithBody(sUrl, "DELETE", headers, data);
    }

    public static HttpTx httpRequestWithBody(String sUrl, String method, Map<String, String> headers, byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SimpleStreamHttpUtils.StreamHttpTx tx = SimpleStreamHttpUtils.http(
                new SimpleStreamHttpUtils.SimpleStreamHttpRequest()
                        .withUrl(sUrl)
                        .withMethod(method)
                        .withHeaders(headers)
                        .withSource(data)
                        .withDestination(baos)
        );
        HttpTx rval = null;
        if (tx!=null)
        {
            rval = (HttpTx)tx.copyTo(new HttpTx());
            rval.setBodyContents(baos.toByteArray());
        }
        return rval;
    }

    /**
     * Attempts up to 'retries' times to read the contents of a URL (times out in timeoutMS if
     * cannot read) - throws an IllegalStateException if it cannot successfully read
     * NOTE: This function properly handles Content-Encoding: GZIP
     *
     * @param urlString   String containing the URL to fetch
     * @param timeoutInMS int containing the # of ms to wait for the url
     * @param retries     int containing the number of times to retry reading the url
     * @return byte[] containing the body from the HTTP request
     */
    public static HttpTx quietFetchUrlDetails(String urlString, int timeoutInMS, int retries) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SimpleStreamHttpUtils.StreamHttpTx tx = SimpleStreamHttpUtils.http(
                new SimpleStreamHttpUtils.SimpleStreamHttpRequest()
                        .withUrl(urlString)
                        .withMethod("GET")
                        .withReadTimeout(timeoutInMS)
                        .withTries(retries+1)
                        .withDestination(baos)
        );
        HttpTx rval = null;
        if (tx!=null)
        {
            rval = (HttpTx)tx.copyTo(new HttpTx());
            rval.setBodyContents(baos.toByteArray());
        }
        return rval;
    }

    /*
     * Like the byte[] version, but wrapped in a String object
     */
    public static String quietFetchUrlAsString(String urlString, int timeout, int retries) {
        return new String(quietFetchUrl(urlString, timeout, retries));
    }

    public static class HttpTx extends SimpleStreamHttpUtils.StreamHttpTx{
        private byte[] bodyContents;

        public byte[] getBodyContents() {
            return bodyContents;
        }

        public void setBodyContents(byte[] bodyContents) {
            this.bodyContents = bodyContents;
        }
    }
}
