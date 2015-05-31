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
 * This filter acts as a switch to decide which provider to do the Oauth dance with, using the
 * following logic:
 * 1) If there are no providers, it throws an exception
 * 2) If there is exactly one provider, it uses that one
 * 3) If there is more than one, and there is a "provider" parameter, it uses that one
 * 4) If there is more than one, and no provider parameter, it redirects to the "selector" url
 *  (which should be unsecured!)
 * 5) If there is a provider parameter with no matching provider, it throws an exception
 *
 * Once the provider is selected, it generates the redirect URL, stores the return URL and
 * nonce in session, and redirects to the redirect URL
 *
 * <p/>
 * Created by chrweiss on 1/2/15.
 */
public class DynamicOauthLoginFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(DynamicOauthLoginFilter.class);

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

            String newContent = content.replaceAll("\\{contextPath\\}",servletRequest.getServletContext().getContextPath());

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
