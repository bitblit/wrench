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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Defaults to requesting email rights
 * Created by chrweiss on 5/26/15.
 */
public class FacebookProvider implements OauthProvider {
    private static final Logger LOG = LoggerFactory.getLogger(FacebookProvider.class);
    public String facebookClientId;
    public String facebookClientSecret;
    private ObjectMapper objectMapper;
    private Set<String> grantedScopes =  new TreeSet<>(Arrays.asList("email"));
    //https://developers.facebook.com/docs/facebook-login/permissions/v2.3#reference


    @Override
    public String getName() {
        return ProviderUtils.defaultProviderRegistryName(getClass());
    }

    public String createEndpoint(String returnUri,String nonce)
    {
        StringBuilder sb = new StringBuilder();
        //response_type=token&
        sb.append(String.format("https://www.facebook.com/dialog/oauth?client_id=%s&redirect_uri=%s",facebookClientId, UTF8Encoder.encode(returnUri)));
        if (nonce!=null)
        {
            sb.append("&state=").append(nonce);
        }
        if (grantedScopes!=null && grantedScopes.size()>0)
        {
            sb.append("&scope=");
            for (Iterator<String> i = grantedScopes.iterator();i.hasNext();)
            {
                sb.append(UTF8Encoder.encode(i.next()));
                if (i.hasNext())
                {
                    sb.append(",");
                }
            }
        }

        return sb.toString();
    }

    public OauthToken createToken(ServletRequest request, ServletResponse response)
    {
        HttpServletRequest r = (HttpServletRequest)request;
        String code = r.getParameter("code");
        return new OauthToken(code);
    }

    public OauthPrincipal validate(OauthToken token, String serviceURL)
    {
        OauthPrincipal rval = new OauthPrincipal();
        String validateURL = "https://graph.facebook.com/v2.3/oauth/access_token?client_id=%s&redirect_uri=%s&client_secret=%s&code=%s";
        String u = String.format(validateURL, facebookClientId, UTF8Encoder.encode(serviceURL), facebookClientSecret, token.getCredentials());
        rval.getOtherData().putAll(ProviderUtils.httpGetUrlParseJsonBody(u,objectMapper));
        return rval;
    }

    @Override
    public void fetchUserData(OauthPrincipal principal) {
        String accessToken = (String)principal.getOtherData().get("access_token");
        if (accessToken!=null) {
            LOG.debug("Fetching user data from Facebook");
            String meUrl = "https://graph.facebook.com/v2.3/me?access_token="+accessToken;
            principal.getOtherData().putAll(ProviderUtils.httpGetUrlParseJsonBody(meUrl, objectMapper));
        }

    }

    public void setFacebookClientId(String facebookClientId) {
        this.facebookClientId = facebookClientId;
    }

    public void setFacebookClientSecret(String facebookClientSecret) {
        this.facebookClientSecret = facebookClientSecret;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setGrantedScopes(Set<String> grantedScopes) {
        this.grantedScopes = grantedScopes;
    }

    public void addGrantedScope(String scope)
    {
        if (scope!=null) {
            if (grantedScopes == null) {
                grantedScopes = new TreeSet<>();
            }
            grantedScopes.add(scope);
        }
    }
}
