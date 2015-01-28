package com.erigir.wrench.shiro.google;

/**
 * Created by chrweiss on 6/28/14.
 */
public class InvalidGoogleTokenException extends RuntimeException {
    private String token;

    public InvalidGoogleTokenException(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
