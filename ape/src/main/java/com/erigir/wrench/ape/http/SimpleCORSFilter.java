package com.erigir.wrench.ape.http;

import com.erigir.wrench.web.AbstractSimpleFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author cweiss
 *
 */
public class SimpleCORSFilter extends AbstractSimpleFilter {
    private static Logger LOG = LoggerFactory.getLogger(SimpleCORSFilter.class);
    // By default accepts all servers
    private Set<Pattern> allowedOrigins = new HashSet<>(Arrays.asList(Pattern.compile(".*")));
    private AllowOriginMode allowOriginMode=AllowOriginMode.SAME_AS_ORIGIN;
    private String corsAllowMethods;
    private Integer corsMaxAge;
    private String corsAllowHeaders;

    @Override
    public void innerFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
        String origin = StringUtils.trimToNull(req.getHeader("Origin"));

        String acceptOrigin = null;
        acceptOrigin = allowOriginMode.createAcceptOrigin(origin, allowedOrigins);

        addCorsHeaders(resp, acceptOrigin);
        String method = req.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            handleOptionsRequest(req, resp);
        } else {
            chain.doFilter(req, resp);
        }
    }


    private void handleOptionsRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // Options requests basically always just get a set of headers and an empty object
        resp.setContentType("application/json");
        resp.setContentLength(2);
        PrintWriter pw = resp.getWriter();
        pw.print("{}");
        pw.flush();

    }

    private void addCorsHeaders(HttpServletResponse response, String origin) {
        if (origin==null)
        {
            LOG.trace("Not setting CORS headers, null origin");
        }
        else
        {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", corsAllowMethods);
            response.setHeader("Access-Control-Max-Age", String.valueOf(corsMaxAge));
            response.setHeader("Access-Control-Allow-Headers", corsAllowHeaders);
        }
    }

    public void setCorsAllowMethods(String corsAllowMethods) {
        this.corsAllowMethods = corsAllowMethods;
    }

    public void setCorsMaxAge(Integer corsMaxAge) {
        this.corsMaxAge = corsMaxAge;
    }

    public void setCorsAllowHeaders(String corsAllowHeaders) {
        this.corsAllowHeaders = corsAllowHeaders;
    }

    public void setAllowedOrigins(Set<Pattern> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public void setAllowOriginMode(AllowOriginMode allowOriginMode) {
        this.allowOriginMode = allowOriginMode;
    }

    public enum AllowOriginMode
    {
        WILDCARD_ALLOW_NO_ORIGIN,
        WILDCARD,
        SAME_AS_ORIGIN;

        public String createAcceptOrigin(String sourceOrigin, Set<Pattern> acceptedOrigins)
        {
            String rval = null;
            if (this==WILDCARD_ALLOW_NO_ORIGIN)
            {
                rval = "*"; // no point in checking the origin
            }
            else
            {
                if (SimpleCORSFilter.matchesAtLeastOne(acceptedOrigins, sourceOrigin))
                {
                    rval = (this==WILDCARD)?"*":sourceOrigin;
                }
            }

            return rval;
        }

    }
}
