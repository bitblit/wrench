package com.erigir.wrench.web.sssi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

/**
 * TODO: Implement in such a way that this can be integrated with Seedy so that it can be a filter at dev time
 * and then combined and pushed to S3 at deploy time
 *
 * Created by cweiss on 7/24/15.
 */
public class SimpleServerSideIncludesFilter implements Filter{
    private static final Logger LOG = LoggerFactory.getLogger(SimpleServerSideIncludesFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        LOG.debug("About to do sssi filter");
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
