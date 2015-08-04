package com.erigir.wrench.ape.http;

import com.erigir.wrench.ape.exception.NoSuchVersionException;
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
public class VersionCheckFilter extends AbstractApeFilter {
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

    public void setValidVersions(List<Integer> validVersions) {
        this.validVersions = validVersions;
    }
}
