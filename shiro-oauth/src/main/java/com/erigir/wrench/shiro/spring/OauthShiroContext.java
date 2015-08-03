package com.erigir.wrench.shiro.spring;


import com.erigir.wrench.shiro.DefaultOauthCustomPrincipalBuilder;
import com.erigir.wrench.shiro.DynamicOauthLoginFilter;
import com.erigir.wrench.shiro.OauthCustomPrincipalBuilder;
import com.erigir.wrench.shiro.OauthFilter;
import com.erigir.wrench.shiro.OauthRealm;
import com.erigir.wrench.shiro.OauthSimpleOutputFilter;
import com.erigir.wrench.shiro.OauthSubjectFactory;
import com.erigir.wrench.shiro.provider.FacebookProvider;
import com.erigir.wrench.shiro.provider.GoogleProvider;
import com.erigir.wrench.shiro.provider.OauthProvider;
import com.erigir.wrench.shiro.provider.ProviderRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.filter.authc.PassThruAuthenticationFilter;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.apache.shiro.web.filter.authz.SslFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Holds the set of beans necessary to add Shiro security implemented via OAuth
 * Created by chrweiss on 5/26/15.
 */
@Configuration
public class OauthShiroContext {
    private static final Logger LOG = LoggerFactory.getLogger(OauthShiroContext.class);

    /**
     * If set to true, the server will use the Host and X-Forwarded-Proto headers if they
     * are available (for Nginx/other proxying) Defaults to true
     * @return whether to use proxy headers when available
     */
    @Bean
    public boolean useProxyHeaders()
    {
        return true;
    }

    /**
     * The list of URLs to bypass security on : by default, favicon.ico, /static/**, /health-check, and the providerSelectorUrl (whatever it is)
     *
     * @return List of strings
     */
    @Bean
    public List<String> bypassUrlList() {
        List<String> bean = Arrays.asList("/favicon.ico", "/static/**", "/health-check", providerSelectorUrl());
        return bean;
    }

    /**
     * Override me to add role and permission mappings
     *
     * @return Map of urls to shiro restrictions
     */
    @Bean
    public Map<String, String> extraShiroUrlMappings() {
        return new TreeMap<String, String>();
    }

    /**
     * Where to mount the OAuth listener, defaults to shiro-oauth (leave it alone)
     *
     * @return String containing the endpoing
     */
    @Bean
    public String oauthServiceEndpoint() {
        return "shiro-oauth";
    }

    /**
     * Where to redirect on oauth failure like a bad ticket, defaults to /oauth-failure (leave it alone)
     *
     * @return String containing the endpoint
     */
    @Bean
    public String failureUrl() {
        return "/oauth-failure";
    }

    /**
     * Where you want to redirect back to after successful login (unless a target was already
     * selected by a direct access attempt).  Defaults to /index.html
     *
     * @return String containing the endpoint
     */
    @Bean
    public String loginSuccessUrl() {
        return "/index.html";
    }

    /**
     * What to display if the user is logged in but lacks correct roles, defaults to /unauthorized
     *
     * @return String containing the endpoint
     */
    @Bean
    public String unauthorizedUrl() {
        return "/unauthorized";
    }

    /**
     * Url that should be hit to initiate Single-Log-Out, defaults to /logout, leave it alone
     *
     * @return String containing the endpoint
     */
    @Bean
    public String logoutUrl() {
        return "/logout";
    }

    /**
     * Url that should be hit to initiate Single-Log-In, defaults to /login, leave it alone
     *
     * @return String containing the endpoint
     */
    @Bean
    public String loginUrl() {
        return "/login";
    }

    /**
     * If your site uses more than one oauth provider (eg, Facebook OR google) then this is the
     * URL that shows all the buttons for the various providers, which should all link
     * to {loginUrl}?p={providerName}, where providerName is the name that provider
     * is registerd under in the provider registry.  By default this would be the simple name of the
     * provider class minus the word "provider", lowercase (eg, FacebookProvider is in there as facebook),
     * See ProviderUtils.defaultProviderRegistryName
     *
     * This URL MUST BE UNAUTHENTICATED OR NOTHING WILL WORK
     *
     * @return String containing the endpoint
     */
    @Bean
    public String providerSelectorUrl() {
        return "/oauth-provider-selector";
    }

    /**
     * Where to redirect to after successful logout
     *
     * @return String containing the endpoint
     */
    @Bean
    public String afterLogoutUrl() {
        return "/logged-out";
    }

    /**
     * SSL port - the user will be redirected here if they aren't running SSL
     *
     * @return String containing the port
     */
    @Bean
    public String sslPort() {
        String port = System.getProperty("shiro.https.port");
        port = (port == null) ? "443" : port;
        LOG.info("Shiro will use HTTPS redirect to port {}", port);
        return port;
    }

    /**
     * Helper bean to simplify the filter chain
     *
     * @return String containing the combined entry
     */
    @Bean
    public String sslConfigEntry() {
        return "ssl[" + sslPort() + "]";
    }

    /**
     * Builds the map from URLs to filter chains
     *
     * @return Map containing the filter definition
     */
    @Bean
    public Map<String, String> filterChainDefinitionMap() {
        LinkedHashMap<String, String> bean = new LinkedHashMap<String, String>();

        // Order is important here!

        // oauth url handled by oauth
        bean.put("/" + oauthServiceEndpoint(), sslConfigEntry() + ", oauth");
        bean.put(failureUrl(), "oauthFailure");

        // Logout url handled by logout - afterLogout url is UNAUTHENTICATED, on purpose
        bean.put(logoutUrl(), "logout");
        bean.put(loginUrl(), "login");
        bean.put(afterLogoutUrl(), "afterLogout");

        // Default unauthorized handler
        // Note - this is the string constant because, if the user overrides then we dont need this mapping anymore
        // so we should NOT map the users custom unauthorized url to what is just some default content
        bean.put("/unauthorized", "unauthorized");

        // Anything from the bypass list is ok
        for (String s : bypassUrlList()) {
            bean.put(s, "anon");
        }

        // Add any explicit mappings (with ssl added)
        for (Map.Entry<String, String> e : extraShiroUrlMappings().entrySet()) {
            bean.put(e.getKey(), e.getValue() + ", " + sslConfigEntry());
        }

        // Everything else caught by the auth filter
        bean.put("/**", "auth, " + sslConfigEntry());

        LOG.debug("The final shiro URL mapping set is : {}", bean);

        return bean;
    }

    /**
     * Sets up the shiro filter
     *
     * @return Filter object for the ShiroFilter
     */
    @Bean
    public Filter shiroFilter() {
        LOG.debug("Creating shiro filter");
        try {

            ShiroFilterFactoryBean factory = new ShiroFilterFactoryBean();
            factory.setFilterChainDefinitionMap(filterChainDefinitionMap());
            factory.setSuccessUrl(loginSuccessUrl());
            factory.setUnauthorizedUrl(unauthorizedUrl());

            // Manually add the Filters
            factory.getFilters().put("oauth", oauthFilter());
            factory.getFilters().put("roles", roleFilter());
            factory.getFilters().put("perms", permissionsAuthorizationFilter());
            factory.getFilters().put("logout", logoutFilter());
            factory.getFilters().put("login", loginFilter());
            factory.getFilters().put("ssl", sslFilter());
            factory.getFilters().put("auth", passThruAuthenticationFilter());
            factory.getFilters().put("anon", new AnonymousFilter());
            factory.getFilters().put("oauthFailure", oauthFailureFilter());
            factory.getFilters().put("afterLogout", logoutSuccessfulFilter());
            factory.getFilters().put("unauthorized", unauthorizedFilter());

            LOG.debug("Shiro Filters are: {}", factory.getFilters());

            factory.setSecurityManager(securityManager());

            return (Filter) factory.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Sets up the permissions auth filter - just need to set the login url
     *
     * @return PermissionsAuthorizationFilter
     */
    @Bean
    public PermissionsAuthorizationFilter permissionsAuthorizationFilter() {
        PermissionsAuthorizationFilter bean = new PermissionsAuthorizationFilter();
        bean.setLoginUrl(loginUrl());
        return bean;
    }

    /**
     * Sets up the default requires-auth filter - just need to set the login url
     * (and success url, if there is one)
     *
     * @return PassThruAuthenticationFilter
     */
    @Bean
    public PassThruAuthenticationFilter passThruAuthenticationFilter() {
        PassThruAuthenticationFilter bean = new PassThruAuthenticationFilter();
        String successUrl = loginSuccessUrl();
        if (successUrl != null) {
            bean.setSuccessUrl(successUrl);
        }
        bean.setLoginUrl(loginUrl());
        return bean;
    }

    /**
     * Sets up the ssl filter - forces users to ssl
     *
     * @return SslFilter
     */
    @Bean
    public SslFilter sslFilter() {
        SslFilter bean = new SslFilter();
        return bean;
    }

    /**
     * Sets up the logout filter (implements single log-out)
     *
     * @return LogoutFilter
     */
    @Bean
    public LogoutFilter logoutFilter() {
        LogoutFilter bean = new LogoutFilter();
        if (afterLogoutUrl() != null) {
            bean.setRedirectUrl(afterLogoutUrl());
        }
        return bean;
    }

    /**
     * Sets up the security manager - hooks in the right subject factory and realms
     *
     * @return SecurityManager
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager bean = new DefaultWebSecurityManager();
        bean.setSubjectFactory(oauthSubjectFactory());
        bean.setRealm(oauthRealm());
        bean.setCacheManager(new MemoryConstrainedCacheManager());
        return bean;
    }

    /**
     * Adds the lifecyclebeanpostprocessor - only used by AOP security annotations
     *
     * @return LifecycleBeanPostProcessor
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        LifecycleBeanPostProcessor bean = new LifecycleBeanPostProcessor();
        return bean;
    }

    /**
     * Sets up the OauthFilter that listens for returning tokens
     *
     * @return OauthFilter
     */
    @Bean
    public OauthFilter oauthFilter() {
        OauthFilter bean = new OauthFilter();
        bean.setFailureUrl(failureUrl());
        bean.setProviderRegistry(providerRegistry());
        return bean;
    }

    /**
     * Sets up the OauthRealm that can process oauth tokens
     *
     * @return OauthRealm
     */
    @Bean
    public OauthRealm oauthRealm() {
        OauthRealm bean = new OauthRealm();
        bean.setProviderRegistry(providerRegistry());
        bean.setOauthCustomPrincipalBuilder(oauthCustomPrincipalBuilder());

        return bean;
    }

    /**
     * A factory for creating subjects from oauth tokens
     *
     * @return OauthSubjectFactory
     */
    @Bean
    public OauthSubjectFactory oauthSubjectFactory() {
        OauthSubjectFactory bean = new OauthSubjectFactory();
        return bean;
    }

    /**
     * Filter for checking if a user has a role - needed to force redirect to oauth provider on a Shiro role-check
     * (as opposed to an 'authenticated' check)
     * Needed mainly just to set the login url
     *
     * @return RolesAuthorizationFilter
     */
    @Bean
    public RolesAuthorizationFilter roleFilter() {
        RolesAuthorizationFilter bean = new RolesAuthorizationFilter();
        bean.setLoginUrl(loginUrl());
        return bean;
    }

    /**
     * Handles serving default output on oauth failure
     *
     * @return OauthSimpleOutputFilter for serving default content
     */
    @Bean
    public OauthSimpleOutputFilter oauthFailureFilter() {
        OauthSimpleOutputFilter bean = new OauthSimpleOutputFilter();

        StringBuilder sb = new StringBuilder();
        sb.append("There was a failure trying to log in.  <a href=\"{contextPath}");
        sb.append(logoutUrl());
        sb.append("\">Try logging out and back in</a>");

        bean.setContent(sb.toString());
        return bean;
    }

    /**
     * Handles serving default output on successful logout
     *
     * @return OauthSimpleOutputFilter for serving default content
     */
    @Bean
    public OauthSimpleOutputFilter logoutSuccessfulFilter() {
        OauthSimpleOutputFilter bean = new OauthSimpleOutputFilter();

        StringBuilder sb = new StringBuilder();
        sb.append("You have been logged out.  <a href=\"{contextPath}");
        sb.append(loginSuccessUrl());
        sb.append("\">Log back in.</a>");

        bean.setContent(sb.toString());
        return bean;
    }

    /**
     * Handles serving default output on insufficient roles for the requested page
     *
     * @return OauthSimpleOutputFilter for serving default content
     */
    @Bean
    public OauthSimpleOutputFilter unauthorizedFilter() {
        OauthSimpleOutputFilter bean = new OauthSimpleOutputFilter();

        StringBuilder sb = new StringBuilder();
        sb.append("You are not authorized to see this page");
        bean.setContent(sb.toString());
        return bean;
    }

    @Bean
    public ObjectMapper shiroObjectMapper() {
        ObjectMapper bean = new ObjectMapper();
        return bean;
    }

    @Bean
    public DynamicOauthLoginFilter loginFilter() {
        DynamicOauthLoginFilter bean = new DynamicOauthLoginFilter();
        bean.setProviderRegistry(providerRegistry());
        bean.setProviderSelectorUrl(providerSelectorUrl());
        bean.setOauthServiceEndpoint(oauthServiceEndpoint());
        bean.setUseProxyHeaders(useProxyHeaders());
        return bean;
    }

    /**
     * Sets up the provider registry - will use beans named "facebookProvider" and
     * "googleProvider" if you have configured them, or if you have set these system properties:
     * shiro.facebook.client.id, shiro.facebook.client.secret, shiro.facebook.client.scope (optional, default=email)
     * and/or
     * shiro.google.client.id, shiro.google.client.secret, shiro.google.client.scope (optional, default=email,openid)
     *
     * @return ProviderRegistry, which has been autoconfigured
     */
    @Bean
    public ProviderRegistry providerRegistry() {
        ProviderRegistry bean = new ProviderRegistry();

        // Setup facebook from props (if set)
        String fbClientId = System.getProperty("shiro.facebook.client.id");
        String fbClientSecret = System.getProperty("shiro.facebook.client.secret");
        String fbScope = System.getProperty("shiro.facebook.scope");
        if (fbClientId != null && fbClientSecret != null) {
            LOG.info("Auto-configuring a facebook implementation for oauth from properties");
            FacebookProvider fb = new FacebookProvider();
            fb.setObjectMapper(shiroObjectMapper());
            fb.setFacebookClientId(fbClientId);
            fb.setFacebookClientSecret(fbClientSecret);
            if (fbScope != null) {
                fb.setGrantedScopes(new TreeSet<String>(Arrays.asList(fbScope.split(","))));
            }
            bean.addProvider(fb);
        }

        // Setup google from props (if set)
        String googClientId = System.getProperty("shiro.google.client.id");
        String googClientSecret = System.getProperty("shiro.google.client.secret");
        String googScope = System.getProperty("shiro.google.scope");
        if (googClientId != null && googClientSecret != null) {
            LOG.info("Auto-configuring a google implementation for oauth from properties");
            GoogleProvider gp = new GoogleProvider();
            gp.setObjectMapper(shiroObjectMapper());
            gp.setGoogleClientId(googClientId);
            gp.setGoogleClientSecret(googClientSecret);
            if (googScope != null) {
                gp.setGrantedScopes(new TreeSet<String>(Arrays.asList(googScope.split(","))));
            }
            bean.addProvider(gp);
        }

        LOG.info("Adding {} providers from bean named 'shiroOauthProviders'",shiroOauthProviders().size());
        for (OauthProvider a:shiroOauthProviders())
        {
            bean.addProvider(a);
        }

        LOG.info("Total of {} oauth providers setup",bean.getProviderMap().size());

        if (bean.getProviderMap().size()==0)
        {
            throw new IllegalStateException("Cannot continue - no providers are configured");
        }
        return bean;
    }

    /**
     * This is a placeholder - you should replace it with your own implementations
     * @return
     */
    @Bean
    public List<OauthProvider> shiroOauthProviders()
    {
        return Collections.emptyList();
    }

    /**
     * Override this with your own principal builder if you want users to have roles/privs other
     * than oauth-user and oauth:*
     *
     * @return OauthCustomPrincipalBuilder
     */
    @Bean
    public OauthCustomPrincipalBuilder oauthCustomPrincipalBuilder() {
        DefaultOauthCustomPrincipalBuilder bean = new DefaultOauthCustomPrincipalBuilder();
        bean.setDefaultPermissions(new TreeSet<String>(Arrays.asList("oauth:*")));
        bean.setDefaultRoles(new TreeSet<String>(Arrays.asList("oauth-user")));
        return bean;
    }
}

