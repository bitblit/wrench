package com.erigir.wrench.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * This filter keeps track of how many times a url has been hit, and when it was hit last.
 *
 * A list of urls to track is maintained and checked, updated on a per request basis.  You
 * may also set a 'reportingPattern', which is a URL which if accessed will dump the current
 * contents of the system.
 * 
 * If you are going to use this filter you'll need to add the following dependency:
          &lt;dependency&gt;
 &lt;groupId&gt;com.fasterxml.jackson.core&lt;/groupId&gt;
 &lt;artifactId&gt;jackson-databind&lt;/artifactId&gt;
 &lt;version&gt;${jackson.version}&lt;/version&gt;
 &lt;/dependency&gt;

 *
 * Created by chrweiss on 3/13/2015.
 */
public class HitMeasuringFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(HitMeasuringFilter.class);
    private List<HitMeasuringEntry> trackingList = new LinkedList<>();

    private Map<HitMeasuringEntry, Date> lastHit = new ConcurrentHashMap<>();
    private Map<HitMeasuringEntry, AtomicInteger> hitCount = new ConcurrentHashMap<>();

    // If this is set, requests matching this pattern will get the status dump instead
    private Pattern reportingPattern;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)servletRequest;

        for (HitMeasuringEntry h:trackingList)
        {
            if (h.matches(req))
            {
                lastHit.put(h, new Date());
                AtomicInteger a = hitCount.get(h);
                a = (a==null)?new AtomicInteger(0):a;
                a.incrementAndGet();
                hitCount.put(h,a);
            }
        }

        if (reportingPattern!=null  && reportingPattern.matcher(req.getRequestURI()).matches())
        {
            doReport((HttpServletResponse)servletResponse);
        }
        else
        {
            filterChain.doFilter(servletRequest,servletResponse);
        }

    }

    @Override
    public void destroy() {
        // Do nothing
    }

    public void doReport(HttpServletResponse resp)
            throws IOException
    {
        resp.setContentType("application/json");
        String out = objectMapper.writeValueAsString(trackingList);
        resp.setContentLength(out.length());
        resp.getWriter().print(out);
    }

    public void setTrackingList(List<HitMeasuringEntry> trackingList) {
        this.trackingList = trackingList;
        if (trackingList==null)
        {
            this.trackingList = Collections.EMPTY_LIST;
        }
    }

    public Map<HitMeasuringEntry, Date> getLastHit() {
        return Collections.unmodifiableMap(lastHit);
    }

    public Map<HitMeasuringEntry, AtomicInteger> getHitCount() {
        return Collections.unmodifiableMap(hitCount);
    }

    public void setReportingPattern(Pattern reportingPattern) {
        this.reportingPattern = reportingPattern;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
