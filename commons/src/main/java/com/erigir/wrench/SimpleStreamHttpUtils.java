package com.erigir.wrench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

/**
 * A couple of simplistic functions for doing HTTP connections without a full HTTPClient dependency
 * Like my SimpleHttpUtils class, but allows you to set a target stream so that the contents don't
 * get pulled into memory if you don't want them there - most commonly would be used to download
 * a larger file straight to disk.
 * <p>
 * cweiss : 5/25/16
 */
public class SimpleStreamHttpUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleStreamHttpUtils.class);
    private static StreamHttpTx LATEST_ERROR = null;

    /*
     * Yes, yes, I know.  Using this in a multithreaded environment is stupid.  Guess what, not everything is a
     * multithreaded environment, and using this in a single threaded environment is a much simpler interface.
     */
    public static StreamHttpTx latestError() {
        return LATEST_ERROR;
    }

    public static StreamHttpTx http(SimpleStreamHttpRequest request) {
        StreamHttpTx rval = null;
        HttpURLConnection connection = null;
        int tryCount = 0;
        // Make sure the request is usable
        request.validate();

        do {

            try {
                tryCount++;

                LOG.info("{} to {}", request.getMethod(), request.getUrl());

                URL u = new URL(request.getUrl());
                connection = (HttpURLConnection) u.openConnection();
                connection.setConnectTimeout(request.getConnectTimeout());
                connection.setReadTimeout(request.getReadTimeout());
                connection.addRequestProperty("Accept-Encoding", "gzip");

                connection.setDoOutput(request.isBodyToSendPresent());
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod(request.getMethod());
                for (Map.Entry<String, String> e : request.getHeaders().entrySet()) {
                    connection.setRequestProperty(e.getKey(), e.getValue());
                }
                if (request.getSourceLength() != null) {
                    connection.setRequestProperty("Content-Length", String.valueOf(request.getSourceLength()));
                }
                connection.setUseCaches(false);

                if (request.isBodyToSendPresent()) {
                    copyInputToOutput(request.getSource(), connection.getOutputStream());
                }

                if (connection.getInputStream() != null) {
                    InputStream is = connection.getInputStream();
                    if ("gzip".equals(connection.getHeaderField("Content-Encoding"))) {
                        is = new GZIPInputStream(is);
                    }
                    copyInputToOutput(is, request.getDestination());
                }

                rval = new StreamHttpTx();
                rval.setHeaders(convertHeaders(connection.getHeaderFields()));
                rval.setStatus(connection.getResponseCode());

            } catch (Exception e) {
                LOG.error("Error during post", e);
                updateLatestError(connection);
                rval = null;
            } finally {
                cleanupConnection(connection);
            }
        } while (rval == null && tryCount<=request.getTries());

        return rval;
    }

    private static void updateLatestError(HttpURLConnection connection) {
        if (connection != null) {
            try {
                StreamHttpTx update = new StreamHttpTx();
                update.setStatus(connection.getResponseCode());
                update.setHeaders(convertHeaders(connection.getHeaderFields()));
                byte[] bodyData = (connection.getErrorStream() == null) ? new byte[0] : ZipUtils.toByteArray(connection.getErrorStream());
                if ("gzip".equals(connection.getHeaderField("Content-Encoding"))) {
                    int pre = bodyData.length;
                    bodyData = ZipUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(bodyData)));
                    LOG.trace("Decomp {} to {}", pre, bodyData.length);
                }
                update.setErrorBody(new String(bodyData));
                LATEST_ERROR = update;
            } catch (IOException ioe) {
                LOG.warn("Bad - got an IOException while trying to update the latest error");
            }
        }
    }

    private static Map<String, String> convertHeaders(Map<String, List<String>> headersIn) {
        Map<String, String> rval = new TreeMap<>();
        for (Map.Entry<String, List<String>> e : headersIn.entrySet()) {
            if (e.getKey() != null && e.getValue() != null && e.getValue().size() > 0) {
                rval.put(e.getKey(), e.getValue().get(0));
            }
        }
        return rval;
    }

    /**
     * Shuts down the underlying connection and releases all its resources
     * I'm well aware that the following code shuts down everything, including occasionally
     * the underlying TCP connection that would otherwise be cached and/or kept alive
     * (see http 1.1 keepalive).  This object isn't meant for performance object caching -
     * if you need that use the excellent HttpClient library.  This library is for very
     * simplistic url reading, like you would in a thick client situation
     *
     * @param connection HTTPUrlConnection to close
     */
    private static void cleanupConnection(HttpURLConnection connection) {
        if (connection != null) {
            try {
                if (connection.getInputStream() != null) {
                    connection.getInputStream().close();
                }
            } catch (IOException ioe) {
                LOG.trace("Error trying to close input stream", ioe);
            }
            connection.disconnect();
        }
    }

    private static void copyInputToOutput(InputStream inputStream, OutputStream outputStream)
            throws IOException
    {
        byte[] buffer    =   new byte[10*1024];

        for (int length; (length = inputStream.read(buffer)) != -1; ){

            outputStream.write(buffer, 0, length);
            outputStream.flush();
        }
    }

    public static class StreamHttpTx {
        private Map<String, String> headers = new TreeMap<>();
        private int status;
        private String errorBody;

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

        public String getErrorBody() {
            return errorBody;
        }

        public void setErrorBody(String errorBody) {
            this.errorBody = errorBody;
        }

        public StreamHttpTx copyTo(StreamHttpTx tx)
        {
            tx.setErrorBody(errorBody);
            tx.setHeaders(headers);
            tx.setStatus(status);
            return tx;
        }
    }

    public static class SimpleStreamHttpRequest {
        private String method = "GET";
        private Map<String,String> headers = new TreeMap<>();
        private InputStream source;
        private Long sourceLength; // needs to be nullable for unknown length input
        private OutputStream destination;
        private String url;
        private int connectTimeout = 2500; // 2.5 seconds to connect
        private int readTimeout = 25000; // 25 seconds to read
        private int tries = 3;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            withMethod(method);
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            withHeaders(headers);
        }

        public InputStream getSource() {
            return source;
        }

        public void setSource(InputStream source) {
            withSource(source);
        }
        public void setSource(byte[] source) {
            withSource(source);
        }

        public Long getSourceLength() {
            return sourceLength;
        }

        public void setSourceLength(Long sourceLength) {
            this.sourceLength = sourceLength;
        }

        public OutputStream getDestination() {
            return destination;
        }

        public void setDestination(OutputStream destination) {
            this.destination = Objects.requireNonNull(destination);
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            withUrl(url);
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }

        public int getTries() {
            return tries;
        }

        public void setTries(int tries) {
            this.tries = tries;
        }

        public SimpleStreamHttpRequest withMethod(final String method) {
            this.method = Objects.requireNonNull(method).toUpperCase();
            return this;
        }

        public SimpleStreamHttpRequest withHeaders(final Map<String, String> headers) {
            this.headers = Objects.requireNonNull(headers);
            return this;
        }

        public SimpleStreamHttpRequest withHeader(String name, String value) {
            if (name!=null && value!=null)
            {
                this.headers.put(name,value);
            }
            return this;
        }

        public SimpleStreamHttpRequest withSource(final InputStream source) {
            this.source = source;
            return this;
        }

        public SimpleStreamHttpRequest withSource(final byte[] source) {
            this.source = new ByteArrayInputStream(source);
            this.setSourceLength(new Long(source.length));
            return this;
        }

        public SimpleStreamHttpRequest withSourceLength(final Long sourceLength) {
            this.sourceLength = sourceLength;
            return this;
        }

        public SimpleStreamHttpRequest withDestination(final OutputStream destination) {
            this.destination = destination;
            return this;
        }

        public SimpleStreamHttpRequest withUrl(final String url) {
            this.url = Objects.requireNonNull(url);
            return this;
        }

        public boolean isBodyToSendPresent()
        {
            return source!=null;
        }

        public SimpleStreamHttpRequest withConnectTimeout(final int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public SimpleStreamHttpRequest withReadTimeout(final int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public SimpleStreamHttpRequest withTries(final int tries) {
            this.tries = tries;
            return this;
        }

        /**
         * Verifies that we have the bare minimum necessary for a request
         */
        public void validate()
        {
            Objects.requireNonNull(url);
            Objects.requireNonNull(method);
            if ("POST".equals(method) || "PUT".equals(method))
            {
                Objects.requireNonNull(source);
            }

            if (destination==null)
            {
                LOG.warn("Warning - using null output stream for response");
                destination = NullOutputStream.NULL_OUTPUT_STREAM;
            }

        }


    }

}
