package com.erigir.wrench.shiro;

/**
 * Created by chrweiss on 2/2/15.
 */

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;

/**
 * {@link org.apache.shiro.mgt.SubjectFactory Subject} implementation to be used in CAS-enabled applications.
 *
 * @since 1.2
 */
public class OAuthSubjectFactory extends DefaultWebSubjectFactory {

    @Override
    public Subject createSubject(SubjectContext context) {

        //the authenticated flag is only set by the SecurityManager after a successful authentication attempt.
        boolean authenticated = context.isAuthenticated();

        //although the SecurityManager 'sees' the submission as a successful authentication, in reality, the
        //login might have been just a CAS rememberMe login.  If so, set the authenticated flag appropriately:
        if (authenticated) {

            AuthenticationToken token = context.getAuthenticationToken();

            if (token != null && token instanceof OAuthToken) {
                OAuthToken casToken = (OAuthToken) token;
                // set the authenticated flag of the context to true only if the CAS subject is not in a remember me mode
                if (casToken.isRememberMe()) {
                    context.setAuthenticated(false);
                }
            }
        }

        return super.createSubject(context);
    }
}