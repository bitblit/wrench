package com.erigir.wrench.ape.http;

import com.erigir.wrench.ape.exception.ApeExceptionWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Root class for scribe filters
 *
 * @author cweiss
 */
@Component(value = "apeFilter")
public abstract class AbstractApeFilter implements Filter {
    private static Logger LOG = LoggerFactory.getLogger(AbstractApeFilter.class);

    public static final String TIMESTAMP_NAME = "X-SCRIBE-TIMESTAMP";

    private ApeExceptionWriter scribeExceptionWriter;


    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) arg0;
        HttpServletResponse resp = (HttpServletResponse) arg1;
        try {
            innerFilter(request, resp, arg2);
        } catch (Exception ex) {
            if (scribeExceptionWriter == null) {
                LOG.error("This is bad, filter {} doesn't have an exception writer set - rethrowing exception", getClass().getName(), ex);
                throw ex;
            } else {
                scribeExceptionWriter.writeExceptionToResponse(request, resp, ex);
            }
        }

        if (resp.getStatus() >= 400) {
            LOG.warn("Warning - webserver is serving a standard error page (we shouldn't have reached here with a status {}) url is {}", resp.getStatus(), request.getRequestURI());
        }
    }

    public abstract void innerFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException;

    @Override
    public void destroy() {
        // Do nothing
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // Do nothing
    }

    public boolean matchesAtLeastOne(List<Pattern> pattern, String value) {
        boolean rval = false;
        if (pattern != null && value != null) {
            for (Iterator<Pattern> i = pattern.iterator(); i.hasNext() && !rval; ) {
                rval = i.next().matcher(value).matches();
            }
        }
        return rval;
    }

    public void setScribeExceptionWriter(ApeExceptionWriter scribeExceptionWriter) {
        this.scribeExceptionWriter = scribeExceptionWriter;
    }

    public Long getSubmittedTimestamp(HttpServletRequest req) {
        String field = getHeaderOrParam(req, TIMESTAMP_NAME);
        return (field == null) ? null : new Long(field);
    }

    public Long getTimestampSkew(HttpServletRequest req) {
        Long timestamp = getSubmittedTimestamp(req);
        return (timestamp == null) ? null : Math.abs(System.currentTimeMillis() - timestamp);
    }

    public final String removeVersionFromURI(String input) {
        String rval = input;
        Integer foundVersion = null;
        if (input != null && input.startsWith("/v")) {
            int split = input.indexOf("/", 2);
            if (split != -1) {
                rval = input.substring(split);
            }
        }
        return rval;
    }

    public final Integer fetchVersion(String input) {
        Integer rval = null;
        if (input != null && input.startsWith("/v")) {
            int split = input.indexOf("/", 2);
            if (split != -1) {
                String test = input.substring(2, split);
                try {
                    rval = new Integer(test);
                } catch (NumberFormatException nfe) {
                    LOG.warn("Couldn't parse version number {}", test);
                }
            }
        }

        return rval;

    }


    public String getHeaderOrParam(HttpServletRequest wrapped, String name) {
        String rval = wrapped.getHeader(name);
        //LOG.info("hn:{}", Collections.list(wrapped.getHeaderNames()));
        if (rval == null) {
            rval = wrapped.getParameter(name);
        }
        return rval;
    }

}
