package com.erigir.wrench.shiro;


import com.erigir.wrench.shiro.provider.OauthProvider;
import com.erigir.wrench.shiro.provider.ProviderRegistry;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * This filter acts as a switch to decide which provider to do the Oauth dance with, using the
 * following logic:
 * 1) If there are no providers, it throws an exception
 * 2) If there is exactly one provider, it uses that one
 * 3) If there is more than one, and there is a "provider" parameter, it uses that one
 * 4) If there is more than one, and no provider parameter, it redirects to the "selector" url
 * (which should be unsecured!)
 * 5) If there is a provider parameter with no matching provider, it throws an exception
 * <p>
 * Once the provider is selected, it generates the redirect URL, stores the return URL and
 * nonce in session, and redirects to the redirect URL
 * <p>
 * Created by chrweiss on 1/2/15.
 */
public class DynamicOauthLoginFilter implements Filter {
  public static final String DYNAMIC_RETURN_URL_KEY = "shiro-oauth-dynamic-return-url";
  public static final String DYNAMIC_SERVICE_NONCE_KEY = "shiro-oauth-dynamic-service-nonce";
  private static final Logger LOG = LoggerFactory.getLogger(DynamicOauthLoginFilter.class);
  private String proxyHostHeader = "Host";
  private String proxySchemeHeader = "X-Forwarded-Proto";

  private ProviderRegistry providerRegistry;
  private String providerSelectorUrl;
  private String oauthServiceEndpoint;
  // If set, the return url will use the host and X-Forwarded-Proto headers
  private boolean useProxyHeaders;


  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Do nothing
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    if (providerRegistry.isEmpty()) {
      throw new IllegalStateException("Misconfigured - there are no providers in the registry");
    }

    OauthProvider provider = providerRegistry.singleProvider();
    if (provider == null) {
      provider = providerRegistry.getProviderByName(request.getParameter("p"));
    } else {
      LOG.debug("There is only a single provider, using it");
    }

    if (provider == null) {
      // We are still null - need to redirect to the selector url
      if (providerSelectorUrl == null) {
        throw new IllegalStateException("Invalid configuration - no provider selector url set");
      }
      LOG.debug("No provider selected, redirecting to providerSelectorUrl : {}", providerSelectorUrl);
      response.sendRedirect(providerSelectorUrl);
    } else {
      LOG.debug("Using provider {}", provider);
      providerRegistry.storeProviderForSession(provider);
      // Build the urls and store in session
      // Nonce is just there to prevent CSRF attacks
      String nonce = UUID.randomUUID().toString().substring(0, 8);
      SecurityUtils.getSubject().getSession().setAttribute(DYNAMIC_SERVICE_NONCE_KEY, nonce);
      // The URL that the oauth server will redirect back to (typically checked in the token lookup, thats
      // why we store it in session
      String oauthReturnUrl = buildServiceUrl(request);
      SecurityUtils.getSubject().getSession().setAttribute(DYNAMIC_RETURN_URL_KEY, oauthReturnUrl);
      // The URL of the oauth service for us to redirect to
      String oauthServiceUrl = provider.createEndpoint(oauthReturnUrl, nonce);

      LOG.debug("Login - redirecting to oauth server {}", oauthServiceUrl);
      response.sendRedirect(oauthServiceUrl);
    }
  }

  private String buildServiceUrl(ServletRequest request) {
    StringBuilder sb = new StringBuilder();
    HttpServletRequest req = (HttpServletRequest) request;
    dumpHeaders(req);
    sb.append(calculateScheme(req));
    sb.append("://");
    sb.append(calculateHost(req));

    String contextPath = request.getServletContext().getContextPath();
    contextPath = (contextPath == null) ? "" : contextPath;
    sb.append(contextPath);

    if (!oauthServiceEndpoint.startsWith("/") && !contextPath.endsWith("/")) {
      sb.append("/");
    }
    sb.append(oauthServiceEndpoint);
    return sb.toString();
  }

  private void dumpHeaders(HttpServletRequest req) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("--- Headers ---");
      for (String s : Collections.list(req.getHeaderNames())) {
        LOG.debug("{} = {}", s, req.getHeader(s));
      }
      LOG.debug("--- End Headers ---");
    }
  }

  private String calculateScheme(HttpServletRequest req) {
    String rval = null;
    if (useProxyHeaders) {
      rval = req.getHeader(proxySchemeHeader);
    }
    if (rval == null) {
      rval = req.getScheme();
    }
    return rval;
  }

  private String calculateHost(HttpServletRequest req) {
    String rval = null;
    if (useProxyHeaders) {
      rval = req.getHeader(proxyHostHeader);
    }
    if (rval == null) {
      rval = req.getServerName() + ":" + req.getServerPort();
    }
    return rval;
  }

  @Override
  public void destroy() {
    // Do nothing
  }

  public void setOauthServiceEndpoint(String oauthServiceEndpoint) {
    this.oauthServiceEndpoint = oauthServiceEndpoint;
  }

  public void setProviderRegistry(ProviderRegistry providerRegistry) {
    this.providerRegistry = providerRegistry;
  }

  public void setProviderSelectorUrl(String providerSelectorUrl) {
    this.providerSelectorUrl = providerSelectorUrl;
  }

  public void setUseProxyHeaders(boolean useProxyHeaders) {
    this.useProxyHeaders = useProxyHeaders;
  }

  public void setProxyHostHeader(String proxyHostHeader) {
    this.proxyHostHeader = proxyHostHeader;
  }

  public void setProxySchemeHeader(String proxySchemeHeader) {
    this.proxySchemeHeader = proxySchemeHeader;
  }
}
