package com.erigir.wrench.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * A configuration object for the HitMeasuringFilter - finds matching http requests based on uri, params, and headers
 * <p>
 * Created by chrweiss on 3/13/15.
 */
public class HitMeasuringEntry implements Comparable<HitMeasuringEntry> {
    private static final Logger LOG = LoggerFactory.getLogger(HitMeasuringEntry.class);
    public static final String URL_REPORT_KEY = "url";
    public static final String PARAMS_REPORT_KEY = "params";
    public static final String HEADERS_REPORT_KEY = "headers";

    private Pattern uriPattern;
    private Map<String, Pattern> paramPatterns;
    private Map<String, Pattern> headerPatterns;

    public HitMeasuringEntry() {
    }

    public HitMeasuringEntry(Pattern uriPattern, Map<String, Pattern> paramPatterns, Map<String, Pattern> headerPatterns) {
        this.uriPattern = uriPattern;
        this.paramPatterns = paramPatterns;
        this.headerPatterns = headerPatterns;
    }


    public boolean matches(HttpServletRequest req) {
        boolean rval = false;
        String uri = req.getRequestURI();
        if (uriPattern != null) {
            rval = uriPattern.matcher(uri).matches();
            LOG.trace("Tested {} against {} match: {}", uri, uriPattern, rval);
        }
        if (rval && paramPatterns != null) {
            for (Iterator<Map.Entry<String, Pattern>> i = paramPatterns.entrySet().iterator(); i.hasNext() && rval; ) {
                Map.Entry<String, Pattern> e = i.next();
                String value = req.getParameter(e.getKey());
                rval = (value != null && e.getValue().matcher(value).matches());
                LOG.trace("For param {} : Tested {} against {} match: {}", e.getKey(), value, e.getValue(), rval);
            }
        }
        if (rval && headerPatterns != null) {
            for (Iterator<Map.Entry<String, Pattern>> i = headerPatterns.entrySet().iterator(); i.hasNext() && rval; ) {
                Map.Entry<String, Pattern> e = i.next();
                String value = req.getHeader(e.getKey());
                rval = (value != null && e.getValue().matcher(value).matches());
                LOG.trace("For header {} : Tested {} against {} match: {}", e.getKey(), value, e.getValue(), rval);
            }
        }

        LOG.trace("Overall, returning : {}", rval);
        return rval;
    }

    public Pattern getUriPattern() {
        return uriPattern;
    }

    public void setUriPattern(Pattern uriPattern) {
        this.uriPattern = uriPattern;
    }

    public Map<String, Pattern> getParamPatterns() {
        return paramPatterns;
    }

    public void setParamPatterns(Map<String, Pattern> paramPatterns) {
        this.paramPatterns = paramPatterns;
    }

    public Map<String, Pattern> getHeaderPatterns() {
        return headerPatterns;
    }

    public void setHeaderPatterns(Map<String, Pattern> headerPatterns) {
        this.headerPatterns = headerPatterns;
    }

    @Override
    public int hashCode() {
        // Dumb but easy
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // Dumb but easy
        if (obj == null || !HitMeasuringEntry.class.isAssignableFrom(obj.getClass())) {
            return false;
        } else {
            return toString().equals(obj.toString());
        }
    }

    @Override
    public int compareTo(HitMeasuringEntry o) {
        // Dumb but easy
        return toString().compareTo(o.toString());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HME [");

        if (uriPattern != null) {
            sb.append("URI: ").append(uriPattern).append(", ");
        }
        if (paramPatterns != null) {
            SortedMap<String, Pattern> out = (SortedMap.class.isAssignableFrom(paramPatterns.getClass())) ? (SortedMap) paramPatterns : new TreeMap<>(paramPatterns);
            sb.append("PARAM: ").append(out).append(", ");
        }
        if (headerPatterns != null) {
            SortedMap<String, Pattern> out = (SortedMap.class.isAssignableFrom(headerPatterns.getClass())) ? (SortedMap) headerPatterns : new TreeMap<>(headerPatterns);
            sb.append("HEADER: ").append(out).append(", ");
        }
        sb.append(" ]");
        return sb.toString();
    }

    public Map<String, Object> toReportMap() {
        Map<String, Object> rval = new TreeMap<>();
        rval.put(URL_REPORT_KEY, uriPattern);
        rval.put(HEADERS_REPORT_KEY, headerPatterns);
        rval.put(PARAMS_REPORT_KEY, paramPatterns);
        return rval;
    }
}
