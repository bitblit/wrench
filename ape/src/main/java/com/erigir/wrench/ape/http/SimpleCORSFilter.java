package com.erigir.wrench.ape.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author cweiss
 */
@Component(value = "simpleCORSFilter")
public class SimpleCORSFilter extends AbstractApeFilter {
    private static Logger LOG = LoggerFactory.getLogger(SimpleCORSFilter.class);
    private String corsAllowMethods;
    private Integer corsMaxAge;
    private String corsAllowHeaders;

    @Override
    public void innerFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
        // We always add CORS headers..
        // TODO: tighten this down again later after debugging
        // TODO: need to check header settings in account

        addCorsHeaders(resp, "*");
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
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Methods", corsAllowMethods);
        response.setHeader("Access-Control-Max-Age", String.valueOf(corsMaxAge));
        response.setHeader("Access-Control-Allow-Headers", corsAllowHeaders);
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

}
