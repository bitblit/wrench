package com.erigir.wrench.shiro;

import org.apache.shiro.authc.RememberMeAuthenticationToken;

/**
 * Created by chrweiss on 1/27/15.
 */
public class OAuthToken implements RememberMeAuthenticationToken {

    // the token returned by the oauth server
    private String token = null;

    // the user identifier
    private String userId = null;

    // is the user in a remember me mode ?
    private boolean isRememberMe = false;

    public OAuthToken(String token) {
        this.token = token;
    }

    public Object getPrincipal() {
        return userId;
    }

    public Object getCredentials() {
        return token;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRememberMe() {
        return isRememberMe;
    }

    public void setRememberMe(boolean isRememberMe) {
        this.isRememberMe = isRememberMe;
    }
}