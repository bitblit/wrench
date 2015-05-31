package com.erigir.wrench.shiro.provider;

import com.erigir.wrench.shiro.OauthException;
import com.erigir.wrench.shiro.OauthPrincipal;
import com.erigir.wrench.shiro.OauthToken;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Created by chrweiss on 5/26/15.
 */
public interface OauthProvider {
    /**
     * Given the URL that should be returned to after the Oauth, generate the initial browser redirect url
     * @param returnURL
     * @param nonce
     * @return
     */
    String createEndpoint(String returnURL,String nonce);

    /**
     * When the oauth server redirects back to your server, it will have some request parameter that holds
     * the token - this function extracts it and places it inside the oauth token object.
     * @param request
     * @param response
     * @return
     */
    OauthToken createToken(ServletRequest request, ServletResponse response);

    /**
     * This function makes a secondary call to the server, exchanging the oauth token for an access token
     * @param token
     * @param serviceURL
     * @return
     * @throws OauthException
     */
    OauthPrincipal validate(OauthToken token, String serviceURL)
            throws OauthException;

    /**
     * If you got an access token that proves authentication, but typically you also want to use
     * the access token to get more information about the customer (for example, email address) assuming
     * you asked for the correct scopes when you generated the oauth endpoint url - this function should
     * make any calls using the access token (typically stored in the "other data" part of the principal)
     * to request more data from the oauth server.  If none is needed, it can just do nothing - otherwise
     * it should add more data to the principal object
     * @param principal
     */
    void fetchUserData(OauthPrincipal principal);
}
