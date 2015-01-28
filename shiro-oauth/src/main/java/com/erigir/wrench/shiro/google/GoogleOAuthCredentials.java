package com.erigir.wrench.shiro.google;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by chrweiss on 9/22/14.
 */
public class GoogleOAuthCredentials {
    private String clientId;
    private String email;
    private String clientSecret;
    private List<String> redirectUris;
    private List<String> javascriptOrigins;

    public GoogleOAuthCredentials() {
    }

    public GoogleOAuthCredentials(Properties props) {
        super();
        clientId = props.getProperty("oauth.google.client.id");
        email = props.getProperty("oauth.google.email");
        clientSecret = props.getProperty("oauth.google.client.secret");
        redirectUris = Arrays.asList(props.getProperty("oauth.google.redirect.uris").split(","));
        javascriptOrigins = Arrays.asList(props.getProperty("oauth.google.javascript.origins").split(","));
        ;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String findContaining(List<String> input, String contains) {
        String rval = null;
        for (String s : redirectUris) {
            if (s.contains(contains)) {
                rval = s;
            }
        }
        return rval;
    }

    public String findURI(String contains) {
        return findContaining(redirectUris, contains);
    }

    public String findJavascriptOrigin(String contains) {
        return findContaining(javascriptOrigins, contains);
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public List<String> getJavascriptOrigins() {
        return javascriptOrigins;
    }

    public void setJavascriptOrigins(List<String> javascriptOrigins) {
        this.javascriptOrigins = javascriptOrigins;
    }
}
