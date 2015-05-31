package com.erigir.wrench.shiro;

import org.apache.shiro.authc.AuthenticationException;

public class OauthAuthenticationException extends AuthenticationException {

    public OauthAuthenticationException() {
        super();
    }

    public OauthAuthenticationException(String message) {
        super(message);
    }

    public OauthAuthenticationException(Throwable cause) {
        super(cause);
    }

    public OauthAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
