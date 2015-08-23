package com.erigir.wrench.ape.http;

import com.erigir.wrench.ape.exception.NoSuchVersionException;
import com.erigir.wrench.web.AbstractSimpleFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Verify the version is valid
 *
 * @author cweiss
 */
@Component(value = "versionCheckFilter")
public class VersionCheckFilter extends AbstractSimpleFilter {
    private static Logger LOG = LoggerFactory.getLogger(VersionCheckFilter.class);
    private List<Integer> validVersions;

    @Override
    public void innerFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
        Integer foundVersion = fetchVersion(req.getRequestURI());
        if (foundVersion == null || !validVersions.contains(foundVersion)) {
            throw new NoSuchVersionException(validVersions);
        }

        chain.doFilter(req, resp); // Matched a no-key regex, handle publicly
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


    public void setValidVersions(List<Integer> validVersions) {
        this.validVersions = validVersions;
    }
}
