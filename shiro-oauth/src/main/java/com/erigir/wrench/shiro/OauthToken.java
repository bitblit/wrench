package com.erigir.wrench.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * This class represents a token for a CAS authentication (service ticket + user id + remember me).
 *
 * @since 1.2
 */
public class OauthToken implements AuthenticationToken {
    // the token returned by the oauth server
    private String token = null;

    // is the user in a remember me mode ?
    private boolean isRememberMe = false;

    public OauthToken(String token) {
        this.token = token;
    }

    public String getPrincipal() {
        return "oauth-principal";
    }

    public String getCredentials() {
        return token;
    }

    public boolean isRememberMe() {
        return isRememberMe;
    }

    public void setRememberMe(boolean isRememberMe) {
        this.isRememberMe = isRememberMe;
    }
}
