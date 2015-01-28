package com.erigir.wrench.shiro.google;

import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.net.URLEncoder;

/**
 * Implements single logout for OAuth
 * First, logs us out of shiro (destroy local session) then
 * Redirect to the OAuth logout endpoint if any, and tell it to redirect back to the
 * post-logout endpoint
 *
 * Created by chrweiss on 1/9/15.
 */
public class OAuthLogoutFilter extends LogoutFilter {
    private static final Logger LOG = LoggerFactory.getLogger(OAuthLogoutFilter.class);

    private String oauthLogoutEndpoint;
    private String afterLogoutEndpoint;

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        Subject subject = getSubject(request, response);
        String returnRedirectUrl = getRedirectUrl(request, response, subject);


         //try/catch added for SHIRO-298:
         try {
                 subject.logout();
             } catch (SessionException ise) {
                 LOG.debug("Encountered session exception during logout.  This can generally safely be ignored.", ise);
             }

        if (oauthLogoutEndpoint!=null)
        {
            LOG.debug("Redirecting for single logout");
            String redirectUrl = oauthLogoutEndpoint+"?service="+ URLEncoder.encode(afterLogoutEndpoint);
            issueRedirect(request, response, redirectUrl);
        }
        else
        {
           issueRedirect(request,response,afterLogoutEndpoint);
        }

         return false;
    }

    public void setOauthLogoutEndpoint(String oauthLogoutEndpoint) {
        this.oauthLogoutEndpoint = oauthLogoutEndpoint;
    }

    public void setAfterLogoutEndpoint(String afterLogoutEndpoint) {
        this.afterLogoutEndpoint = afterLogoutEndpoint;
    }
}
