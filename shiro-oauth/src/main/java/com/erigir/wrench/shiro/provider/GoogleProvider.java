package com.erigir.wrench.shiro.provider;


import com.erigir.wrench.UTF8Encoder;
import com.erigir.wrench.shiro.OauthPrincipal;
import com.erigir.wrench.shiro.OauthToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Defaults to requesting email and openid rights
 * Created by chrweiss on 5/26/15.
 */
public class GoogleProvider implements OauthProvider {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleProvider.class);
    private String googleClientId;
    private String googleClientSecret;
    private Set<String> grantedScopes = new TreeSet<>(Arrays.asList("openid", "email"));

    private static final String GOOGLE_AUTH = "https://accounts.google.com/o/oauth2/auth";
    private static final String GOOGLE_TOKEN_EXCHANGE = "https://accounts.google.com/o/oauth2/token";
    private static final String GOOGLE_ID_TOKEN_INFO = "https://www.googleapis.com/oauth2/v1/tokeninfo?id_token=%s";
    private static final String GOOGLE_ACCESS_TOKEN_INFO = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=%s";

    private ObjectMapper objectMapper;

    @Override
    public String getName() {
        return ProviderUtils.defaultProviderRegistryName(getClass());
    }

    @Override
    public String createEndpoint(String returnURL, String nonce) {
        StringBuilder sb = new StringBuilder(GOOGLE_AUTH);
        sb.append("?");
        sb.append("client_id=").append(googleClientId);
        sb.append("&").append("response_type=").append("code");
        sb.append("&").append("redirect_uri=").append(UTF8Encoder.encode(returnURL));
        sb.append("&").append("state=").append(UTF8Encoder.encode(nonce));

        if (grantedScopes != null && grantedScopes.size() > 0) {
            sb.append("&").append("scope=");
            StringBuilder sb2 = new StringBuilder();
            for (String s : grantedScopes) {
                sb2.append(s);
                sb2.append(" ");
            }
            sb.append(UTF8Encoder.encode((sb2.toString().trim())));
        }

        return sb.toString();
    }

    @Override
    public OauthToken createToken(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;

        String state = req.getParameter("state");
        String code = req.getParameter("code");
        String authuser = req.getParameter("authuser");
        String num_sessions = req.getParameter("num_sessions");
        String prompt = req.getParameter("prompt");
        String session_state = req.getParameter("session_state");

        return new OauthToken(code);
    }

    public OauthPrincipal validate(OauthToken token, String serviceURL) {
        OauthPrincipal rval = new OauthPrincipal();

        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("code", token.getCredentials());
        body.put("client_id", googleClientId);
        body.put("client_secret", googleClientSecret);
        body.put("redirect_uri", serviceURL);
        body.put("grant_type", "authorization_code");

        rval.getOtherData().putAll(ProviderUtils.httpPostUrlParseJsonBody(GOOGLE_TOKEN_EXCHANGE, body, objectMapper));


        return rval;
    }

    @Override
    public void fetchUserData(OauthPrincipal principal) {
        String accessToken = (String) principal.getOtherData().get("access_token");
        if (accessToken != null) {
            LOG.debug("Fetching user data from google");
            principal.getOtherData().putAll(processAccessToken(accessToken));
        }
    }

    public Map<String, Object> processIdToken(String token) {
        return validateToken(String.format(GOOGLE_ID_TOKEN_INFO, token));
    }

    public Map<String, Object> processAccessToken(String token) {
        return validateToken(String.format(GOOGLE_ACCESS_TOKEN_INFO, token));
    }


    private Map<String, Object> validateToken(String url) {
        return ProviderUtils.httpGetUrlParseJsonBody(url, objectMapper);
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
    }

    public void setGoogleClientSecret(String googleClientSecret) {
        this.googleClientSecret = googleClientSecret;
    }

    public void setGrantedScopes(Set<String> grantedScopes) {
        this.grantedScopes = grantedScopes;
    }
}