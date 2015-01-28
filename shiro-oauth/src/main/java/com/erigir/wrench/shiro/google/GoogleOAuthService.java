package com.erigir.wrench.shiro.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

/**
 * Endpoint for handling changes to the key management system
 * <p/>
 * cweiss : 6/28/14 11:53 AM
 */
public class GoogleOAuthService {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleOAuthService.class);

    private String authenticationEndpoint;
    private String tokenExchangeEndpoint;
    private String idTokenInfoEndpoint;
    private String accessTokenInfoEndpoint;

    private GoogleOAuthCredentials credentials;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    /**
     * Handle an openid startup request
     *
     * @return
     * @throws Exception
     */
    public String createStartOAuthURL(String state, String serverName)
            throws Exception {
        // Create a state token to prevent request forgery.
        // Store it in the session for later validation.

        String uri = credentials.findURI(serverName);
        if (uri == null) {
            throw new InvalidOAuthURI(serverName+" valid "+credentials.getRedirectUris());
        }

        StringBuilder sb = new StringBuilder(authenticationEndpoint);
        sb.append("?");
        sb.append("client_id=").append(credentials.getClientId());
        sb.append("&").append("response_type=").append("code");
        sb.append("&").append("scope=").append("openid email");
        sb.append("&").append("redirect_uri=").append(uri);
        sb.append("&").append("state=").append(state);


        return sb.toString();
    }

    /**
     * Handle an openid startup request
     *
     * @return
     * @throws Exception
     */
    public
    String exchangeToken( String serverName,
                                          String code)

            throws Exception {

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", credentials.getClientId());
        body.add("client_secret", credentials.getClientSecret());
        body.add("redirect_uri", credentials.findURI(serverName));
        body.add("grant_type", "authorization_code");

        // Now exchange the code for the token
            Map<String, Object> temp = restTemplate.postForObject(tokenExchangeEndpoint, body, Map.class);
            String idTokenEncoded = (String) temp.get("id_token");
            GoogleIdToken rval = validateIdToken(idTokenEncoded);
            // If we reached here the token is ok
            return idTokenEncoded;
    }

    public GoogleIdToken validateIdToken(String token) {
        return validateToken(idTokenInfoEndpoint, token);
    }

    public GoogleIdToken validateAccessToken(String token) {
        return validateToken(accessTokenInfoEndpoint, token);
    }


    private GoogleIdToken validateToken(String url, String token) {
        try {
            Map<String, String> params = Collections.singletonMap("token", token);
            GoogleIdToken data = restTemplate.getForObject(url, GoogleIdToken.class, params);
            if (data.getClientId() == null || !data.getClientId().equals(credentials.getClientId())) {
                throw new InvalidGoogleTokenException(token);
            }
            return data;
        } catch (HttpClientErrorException hce) {
            LOG.warn("Error trying to validate token", hce);
            return null;
        }
    }

    public void setCredentials(GoogleOAuthCredentials credentials) {
        this.credentials = credentials;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setAuthenticationEndpoint(String authenticationEndpoint) {
        this.authenticationEndpoint = authenticationEndpoint;
    }

    public void setTokenExchangeEndpoint(String tokenExchangeEndpoint) {
        this.tokenExchangeEndpoint = tokenExchangeEndpoint;
    }

    public void setIdTokenInfoEndpoint(String idTokenInfoEndpoint) {
        this.idTokenInfoEndpoint = idTokenInfoEndpoint;
    }

    public void setAccessTokenInfoEndpoint(String accessTokenInfoEndpoint) {
        this.accessTokenInfoEndpoint = accessTokenInfoEndpoint;
    }
}
