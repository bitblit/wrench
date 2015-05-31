package com.erigir.wrench.shiro;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Basically just creates a dumb endpoint as a placeholder for unsecured default content that is
 * unexpected to be served, but needs an endpoint regardless - they can
 * be overridden by implementing different endpoints and adding them to the
 * bypass URL list
 *
 * Created by chrweiss on 1/2/15.
 */
public class OauthSimpleOutputFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(OauthSimpleOutputFilter.class);

    private boolean enabled = true;
    private String content = "Replace Me With Content";
    private String contentType = "text/html";
    private int statusCode = 200;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (enabled) {
            LOG.debug("Default content");
            HttpServletResponse resp = (HttpServletResponse) servletResponse;
            resp.setContentType(contentType);

            String newContent = content.replaceAll("\\{contextPath\\}", servletRequest.getServletContext().getContextPath());

            resp.setContentLength(newContent.length());


            resp.getWriter().print(newContent);
        } else {
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
