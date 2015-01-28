package com.erigir.wrench.shiro.google;

/**
 * Created by chrweiss on 6/28/14.
 */
public class InvalidOAuthURI extends RuntimeException {
    private String uri;

    public InvalidOAuthURI(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
