package com.erigir.wrench.shiro;

import com.erigir.wrench.shiro.provider.OauthProvider;
import com.erigir.wrench.shiro.provider.ProviderRegistry;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * This filter validates the OAuth ticket to authenticate the user.  It must be configured on the URL recognized
 * by the OAuth server.  For example, in {@code shiro.ini}:
 * <pre>
 * [main]
 * oauthFilter = com.erigir.wrench.shiro.OAuthFilter
 * ...
 *
 * [urls]
 * /shiro-oauth = oauthFilter
 * ...
 * </pre>
 * (example : http://host:port/mycontextpath/shiro-oauth)
 *
 */
public class OauthFilter extends AuthenticatingFilter {
    
    private static Logger LOG = LoggerFactory.getLogger(OauthFilter.class);

    private ProviderRegistry providerRegistry;

    // the url where the application is redirected if the Oauth token validation failed (example : /mycontextpatch/oauth_error.jsp)
    private String failureUrl;
    
    /**
     * The token created for this authentication is an OauthToken containing the token received on the Oauth service url (on which
     * the filter must be configured).
     * 
     * @param request the incoming request
     * @param response the outgoing response
     * @throws Exception if there is an error processing the request.
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        return providerRegistry.fetchProviderForSession().createToken(request, response);
    }
    
    /**
     * Execute login by creating {@link #createToken(javax.servlet.ServletRequest, javax.servlet.ServletResponse) token} and logging subject
     * with this token.
     * 
     * @param request the incoming request
     * @param response the outgoing response
     * @throws Exception if there is an error processing the request.
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        LOG.debug("Access denied to {}, executing login", ((HttpServletRequest) request).getRequestURI());
        return executeLogin(request, response);
    }
    
    /**
     * Returns <code>false</code> to always force authentication (user is never considered authenticated by this filter).
     * 
     * @param request the incoming request
     * @param response the outgoing response
     * @param mappedValue the filter-specific config value mapped to this filter in the URL rules mappings.
     * @return <code>false</code>
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return false;
    }
    
    /**
     * If login has been successful, redirect user to the original protected url.
     * 
     * @param token the token representing the current authentication
     * @param subject the current authenticated subjet
     * @param request the incoming request
     * @param response the outgoing response
     * @throws Exception if there is an error processing the request.
     */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request,
                                     ServletResponse response) throws Exception {
        LOG.debug("Successful login - redirecting to original url");
        issueSuccessRedirect(request, response);
        return false;
    }
    
    /**
     * If login has failed, redirect user to the Oauth error page (no token or token validation failed) except if the user is already
     * authenticated, in which case redirect to the default success url.
     * 
     * @param token the token representing the current authentication
     * @param ae the current authentication exception
     * @param request the incoming request
     * @param response the outgoing response
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException ae, ServletRequest request,
                                     ServletResponse response) {
        // is user authenticated or in remember me mode ?
        Subject subject = getSubject(request, response);
        if (subject.isAuthenticated() || subject.isRemembered()) {
            try {
                issueSuccessRedirect(request, response);
            } catch (Exception e) {
                LOG.error("Cannot redirect to the default success url", e);
            }
        } else {
            try {
                WebUtils.issueRedirect(request, response, failureUrl);
            } catch (IOException e) {
                LOG.error("Cannot redirect to failure url : {}", failureUrl, e);
            }
        }
        return false;
    }
    
    public void setFailureUrl(String failureUrl) {
        this.failureUrl = failureUrl;
    }

    public void setProviderRegistry(ProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }
}
