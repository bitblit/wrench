package com.erigir.wrench.shiro.spring;


import com.erigir.wrench.shiro.*;
import com.erigir.wrench.shiro.provider.FacebookProvider;
import com.erigir.wrench.shiro.provider.GoogleProvider;
import com.erigir.wrench.shiro.provider.OauthProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.apache.shiro.web.filter.authz.SslFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.Arrays;
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
     * The list of URLs to bypass security on : by default, favicon.ico, /static/**, and /health-check
     *
     * @return
     */
    @Bean
    public List<String> bypassUrlList() {
        List<String> bean = Arrays.asList("/favicon.ico", "/static/**", "/health-check");
        return bean;
    }

    /**
     * Override me to add role and permission mappings
     *
     * @return
     */
    @Bean
    public Map<String, String> extraShiroUrlMappings() {
        return new TreeMap<String, String>();
    }

    /**
     * Where to mount the OAuth listener, defaults to shiro-oauth (leave it alone)
     *
     * @return
     */
    @Bean
    public String oauthServiceEndpoint() {
        return "shiro-oauth";
    }

    /**
     * Where to redirect on oauth failure like a bad ticket, defaults to /oauth-failure (leave it alone)
     *
     * @return
     */
    @Bean
    public String failureUrl() {
        return "/oauth-failure";
    }

    /**
     * Where you want to redirect back to after successful login (unless a target was already
     * selected by a direct access attempt).  Defaults to /index.html
     *
     * @return
     */
    @Bean
    public String loginSuccessUrl() {
        return "/index.html";
    }

    /**
     * What to display if the user is logged in but lacks correct roles, defaults to /unauthorized
     *
     * @return
     */
    @Bean
    public String unauthorizedUrl() {
        return "/unauthorized";
    }

    /**
     * Url that should be hit to initiate Single-Log-Out, defaults to /logout, leave it alone
     *
     * @return
     */
    @Bean
    public String logoutUrl() {
        return "/logout";
    }

    /**
     * Where to redirect to after successful logout
     *
     * @return
     */
    @Bean
    public String afterLogoutUrl() {
        return "/logged-out";
    }

    /**
     * SSL port - the user will be redirected here if they aren't running SSL
     *
     * @return
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
     * @return
     */
    @Bean
    public String sslConfigEntry() {
        return "ssl[" + sslPort() + "]";
    }

    /**
     * Builds the map from URLs to filter chains
     *
     * @return
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
     * @return
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
            factory.getFilters().put("ssl", sslFilter());
            factory.getFilters().put("auth", oauthPassThruAuthenticationFilter());
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

    @Bean
    public PermissionsAuthorizationFilter permissionsAuthorizationFilter() {
        OauthPermissionsAuthorizationFilter bean = new OauthPermissionsAuthorizationFilter();
        bean.setDynamicReturnUrlBuilder(oauthDynamicReturnUrlBuilder());
        return bean;
    }

    /**
     * Sets up the OauthAuthenticationFilter - used to redirect unauthenticated users to the oauth provider
     *
     * @return
     */
    @Bean
    public OauthPassThruAuthenticationFilter oauthPassThruAuthenticationFilter() {
        OauthPassThruAuthenticationFilter bean = new OauthPassThruAuthenticationFilter();
        bean.setDynamicReturnUrlBuilder(oauthDynamicReturnUrlBuilder());
        String successUrl = loginSuccessUrl();
        if (successUrl != null) {
            bean.setSuccessUrl(successUrl);
        }
        return bean;
    }

    /**
     * Sets up the ssl filter - forces users to ssl
     *
     * @return
     */
    @Bean
    public SslFilter sslFilter() {
        SslFilter bean = new SslFilter();
        return bean;
    }

    /**
     * Sets up the logout filter (implements single log-out)
     *
     * @return
     */
    @Bean
    public LogoutFilter logoutFilter() {
        LogoutFilter bean = new LogoutFilter();
        return bean;
    }

    /**
     * Sets up the security manager - hooks in the right subject factory and realms
     *
     * @return
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
     * @return
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        LifecycleBeanPostProcessor bean = new LifecycleBeanPostProcessor();
        return bean;
    }

    /**
     * Sets up the OauthFilter that listens for returning tokens
     *
     * @return
     */
    @Bean
    public OauthFilter oauthFilter() {
        OauthFilter bean = new OauthFilter();
        bean.setFailureUrl(failureUrl());
        bean.setProviderList(providerList());
        return bean;
    }

    /**
     * Sets up the OauthRealm that can process oauth tokens
     *
     * @return
     */
    @Bean
    public OauthRealm oauthRealm() {
        OauthRealm bean = new OauthRealm();
       bean.setProviderList(providerList());
        bean.setOauthCustomPrincipalBuilder(oauthCustomPrincipalBuilder());

        return bean;
    }

    /**
     * A factory for creating subjects from cas tickets
     *
     * @return
     */
    @Bean
    public OauthSubjectFactory oauthSubjectFactory() {
        OauthSubjectFactory bean = new OauthSubjectFactory();
        return bean;
    }

    /**
     * Filter for checking if a user has a role - needed to force redirect to oauth provider on a Shiro role-check
     * (as opposed to an 'authenticated' check)
     *
     * @return
     */
    @Bean
    public RolesAuthorizationFilter roleFilter() {
        OauthRolesAuthorizationFilter bean = new OauthRolesAuthorizationFilter();
        bean.setDynamicReturnUrlBuilder(oauthDynamicReturnUrlBuilder());
        return bean;
    }

    /**
     * Shared bean for building dynamic urls based on the incoming servlet request)
     *
     * @return
     */
    @Bean
    public OauthDynamicReturnUrlBuilder oauthDynamicReturnUrlBuilder() {
        OauthDynamicReturnUrlBuilder bean = new OauthDynamicReturnUrlBuilder();
        bean.setOauthRealm(oauthRealm());
        bean.setOauthServiceEndpoint(oauthServiceEndpoint());
        bean.setProviderList(providerList());
        return bean;
    }

    /**
     * Handles serving default output on oauth failure
     *
     * @return
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
     * @return
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
     * @return
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
    public ObjectMapper shiroObjectMapper()
    {
        ObjectMapper bean = new ObjectMapper();
        return bean;
    }

    @Bean
    public FacebookProvider facebookProvider()
    {
        FacebookProvider bean = new FacebookProvider();
        bean.setObjectMapper(shiroObjectMapper());
        bean.addGrantedScope("email");
        return bean;
    }

    @Bean
    public GoogleProvider googleProvider()
    {
        GoogleProvider bean = new GoogleProvider();
        bean.setObjectMapper(shiroObjectMapper());
        return bean;
    }

    @Bean
    public List<OauthProvider> providerList()
    {
        return Arrays.asList((OauthProvider)googleProvider());
    }

    @Bean
    public OauthCustomPrincipalBuilder oauthCustomPrincipalBuilder()
    {
        DefaultOauthCustomPrincipalBuilder bean = new DefaultOauthCustomPrincipalBuilder();
        bean.setDefaultPermissions(new TreeSet<String>(Arrays.asList("oauth:*")));
        bean.setDefaultRoles(new TreeSet<String>(Arrays.asList("oauth-user")));
        return bean;
    }
}

