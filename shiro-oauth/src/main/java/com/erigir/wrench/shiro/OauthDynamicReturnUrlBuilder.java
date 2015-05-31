package com.erigir.wrench.shiro;

import com.erigir.wrench.shiro.provider.OauthProvider;
import org.apache.shiro.SecurityUtils;

import javax.servlet.ServletRequest;
import java.util.List;
import java.util.UUID;

/**
 * This class encapsulates the common dynamic url logic
 * <p/>
 * Created by chrweiss on 1/2/15.
 */
public class OauthDynamicReturnUrlBuilder {

    private OauthRealm oauthRealm;
    private String oauthServiceEndpoint;
    private List<OauthProvider> providerList;

    /**
     * Documenting this since its a little weird - once it builds the dynamic service endpoint that
     * OAuth will redirect back to, stores it in the session so that the returned OAuth ticket can
     * be correctly decoded.
     *
     * @param request
     * @return
     * @see OauthRealm
     */
    public String storeServiceUrlAndGenerateReturnUrl(ServletRequest request) {
        String serviceUrl = buildServiceUrl(request);
        String returnUrl = buildReturnUrl(serviceUrl);
        SecurityUtils.getSubject().getSession().setAttribute(oauthRealm.getDynamicServiceUrlKey(), serviceUrl);

        String nonce = UUID.randomUUID().toString().substring(0,16);
        SecurityUtils.getSubject().getSession().setAttribute(oauthRealm.getDynamicServiceNonceKey(), nonce);

        return returnUrl;
    }

    public String buildReturnUrl(String serviceUrl) {
        String nonce = (String)SecurityUtils.getSubject().getSession().getAttribute(oauthRealm.getDynamicServiceNonceKey());
            return selectProvider().createEndpoint(serviceUrl,nonce);

            /*
            StringBuilder sb = new StringBuilder();
            // TODO: fix sb.append(oauthRealm.getOauthServerUrlPrefix());
            sb.append("/login?service=");
            sb.append(URLEncoder.encode(serviceUrl, "UTF-8"));
            return sb.toString();
            */
    }

    public String buildServiceUrl(ServletRequest request) {
        return buildDynamicUrl(request, oauthServiceEndpoint);
    }

    public String buildDynamicUrl(ServletRequest request, String endpoint) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getScheme());
        sb.append("://");
        sb.append(request.getServerName());
        sb.append(":");
        sb.append(request.getServerPort());

        String contextPath = request.getServletContext().getContextPath();
        contextPath = (contextPath==null)?"":contextPath;
        sb.append(contextPath);

        if (!endpoint.startsWith("/") && !contextPath.endsWith("/")) {
            sb.append("/");
        }
        sb.append(endpoint);
        return sb.toString();
    }

    public void setOauthRealm(OauthRealm oauthRealm) {
        this.oauthRealm = oauthRealm;
    }

    public void setOauthServiceEndpoint(String oauthServiceEndpoint) {
        this.oauthServiceEndpoint = oauthServiceEndpoint;
    }

    public void setProviderList(List<OauthProvider> providerList) {
        this.providerList = providerList;
    }

    private OauthProvider selectProvider()
    {
        // TODO: Implement
        return (providerList!=null && providerList.size()>0)?providerList.get(0):null;
    }

}
