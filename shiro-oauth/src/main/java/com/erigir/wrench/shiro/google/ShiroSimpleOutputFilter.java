package com.erigir.wrench.shiro.google;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Basically just creates a dumb endpoint as a placeholder for unsecured default content that is
 * unexpected to be served, but needs an endpoint regardless - they can
 * be overridden by implementing different endpoints and adding them to the
 * bypass URL list
 *
 * This is here since I need default endpoints but I have no idea what rendering system the
 * user is using behind shiro - this allows me to not care.
 *
 * Created by chrweiss on 1/2/15.
 */
public class ShiroSimpleOutputFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(ShiroSimpleOutputFilter.class);

    private boolean enabled = true;
    private String content="Replace Me With Content";
    private String contentType="text/html";
    private int statusCode=200;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (enabled)
        {
            LOG.debug("Handling cas-failure");
            HttpServletResponse resp = (HttpServletResponse)servletResponse;
            resp.setContentType(contentType);
            resp.setContentLength(content.length());
            resp.getWriter().print(content);
        }
        else
        {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
