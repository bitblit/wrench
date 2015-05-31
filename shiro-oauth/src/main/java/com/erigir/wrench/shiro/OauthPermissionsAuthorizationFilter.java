package com.erigir.wrench.shiro;

import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This class is only here to replace the standard loginURL setting with one
 * that is dynamically built from the current request name - allows a single
 * server to serve multiple domains auth'd by OAuth, and removes the need
 * for manually setting the return url
 * <p/>
 * This filter also stores the dynamic url used in session so that the token validator
 * can run afterwards - requires clusterable sessions!
 * <p/>
 * Created by chrweiss on 12/30/14.
 */
public class OauthPermissionsAuthorizationFilter extends PermissionsAuthorizationFilter {
    private static final Logger LOG = LoggerFactory.getLogger(OauthPermissionsAuthorizationFilter.class);
    private OauthDynamicReturnUrlBuilder dynamicReturnUrlBuilder;

    @Override
    protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        if (LOG.isDebugEnabled() && HttpServletRequest.class.isAssignableFrom(request.getClass())) {
            LOG.debug("Redirecting request from {} to login", ((HttpServletRequest) request).getRequestURI());
        }
        String loginUrl = dynamicReturnUrlBuilder.storeServiceUrlAndGenerateReturnUrl(request);
        WebUtils.issueRedirect(request, response, loginUrl);
    }

    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
        boolean rval = super.isAccessAllowed(request, response, mappedValue);
        return rval;
    }

    @Override
    public String getLoginUrl() {
        throw new UnsupportedOperationException("This filter creates the login url dynamically");
    }

    @Override
    public void setLoginUrl(String loginUrl) {
        throw new UnsupportedOperationException("This filter creates the login url dynamically");
    }

    public void setDynamicReturnUrlBuilder(OauthDynamicReturnUrlBuilder dynamicReturnUrlBuilder) {
        this.dynamicReturnUrlBuilder = dynamicReturnUrlBuilder;
    }

}
