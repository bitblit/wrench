package com.erigir.wrench.ape.http;

import com.erigir.wrench.ape.exception.HttpsRequiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Forces HTTPS exception for exclusions
 *
 * @author cweiss
 */
@Component(value = "requireHTTPSFilter")
public class RequireHTTPSFilter extends AbstractApeFilter {
    private static Logger LOG = LoggerFactory.getLogger(RequireHTTPSFilter.class);
    private List<Pattern> excludePatterns;
    private boolean allowProxyTermination = true;
    private String proxyTerminationHeader="X-Forwarded-Proto";

    @Override
    public void innerFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String scheme = req.getScheme();
        String uri = req.getRequestURI();
        boolean proxyTerminated = (allowProxyTermination && proxyTerminationHeader!=null && "https".equalsIgnoreCase(req.getHeader(proxyTerminationHeader)));

        if (!"https".equalsIgnoreCase(scheme) && !proxyTerminated && !matchesAtLeastOne(excludePatterns, uri)) {
            LOG.warn("Non-HTTPS request made (scheme was '{}' uri was '{}')",scheme,uri);
            throw new HttpsRequiredException();
        }

        chain.doFilter(req, resp);
    }

    public void setProxyTerminationHeader(String proxyTerminationHeader) {
        this.proxyTerminationHeader = proxyTerminationHeader;
    }

    public void setAllowProxyTermination(boolean allowProxyTermination) {
        this.allowProxyTermination = allowProxyTermination;
    }

    public void setExcludePatterns(List<Pattern> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }
}
