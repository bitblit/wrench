package com.erigir.wrench.shiro.google;


import com.erigir.wrench.shiro.OAuthFilter;
import com.erigir.wrench.shiro.OAuthPassThruAuthenticationFilter;
import com.erigir.wrench.shiro.OAuthRealm;
import com.erigir.wrench.shiro.OAuthRolesAuthorizationFilter;
import com.erigir.wrench.shiro.OAuthSubjectFactory;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
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

/**
 * This is a spring configuration file!  You must add the spring dependency or this wont work.
 *
 * Created by chrweiss on 7/12/14.
 */
@Configuration
public class SimpleGoogleShiroContext {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleGoogleShiroContext.class);

    /**
     * The list of URLs to bypass security on : by default, favicon.ico, /static/**, and /health-check
     * @return
     */
    @Bean
    public List<String> bypassUrlList()
    {
        List<String> bean = Arrays.asList("/favicon.ico","/static/**","/health-check");
        return bean;
    }

    /**
     * Where to mount the OAuth Return listener, defaults to oauth-return (leave it alone)
     * @return
     */
    @Bean
    public String casServiceEndpoint()
    {
        return "oauth-return";
    }

    /**
     * Where to redirect on oauth failure like a bad ticket, defaults to /oauth-failure (leave it alone)
     * @return
     */
    @Bean
    public String failureUrl()
    {
        return "/oauth-failure";
    }

    /**
     * URL for OAuth server.  Defaults to https://zuul.zappos.com/zuul - leave it alone
     * @return
     */
    @Bean
    public String zuulServerPrefix()
    {
        return "https://zuul.zappos.com/zuul";
    }

    /**
     * Where you want Zuul to redirect back to after successful login.  Defaults to /index.html
     * @return
     */
    @Bean
    public String loginSuccessUrl()
    {
        return "/index.html";
    }

    /**
     * What to display if the user is logged in but lacks correct roles, defaults to /unauthorized
     * @return
     */
    @Bean
    public String unauthorizedUrl()
    {
        return "/unauthorized";
    }

    /**
     * Url that should be hit to initiate Single-Log-Out, defaults to /logout, leave it alone
     * @return
     */
    @Bean
    public String logoutUrl()
    {
        return "/logout";
    }

    /**
     * Where to redirect to after successful logout
     * @return
     */
    @Bean
    public String afterLogoutUrl()
    {
        return "/logged-out";
    }

    /**
     * SSL port - the user will be redirected here if they aren't running SSL
     * @return
     */
    @Bean
    public String sslPort()
    {
        String port = System.getProperty("shiro.https.port");
        return (port==null)?"443":port;
    }
    /**
     * Helper bean to simplify the filter chain
     * @return
     */
    @Bean
    public String sslConfigEntry()
    {
        return "ssl["+sslPort()+"]";
    }

    /**
     * Builds the map from URLs to filter chains
     * @return
     */
    @Bean
    public Map<String,String> filterChainDefinitionMap()
    {
        LinkedHashMap<String,String> bean = new LinkedHashMap<String,String>();

        // Order is important here!

        // CAS url handled by CAS
        bean.put("/"+casServiceEndpoint(),sslConfigEntry()+", cas");
        bean.put(failureUrl(),"casFailure");

        // Logout url handled by logout - afterLogout url is UNAUTHENTICATED, on purpose
        bean.put(logoutUrl(),"logout");
        bean.put(afterLogoutUrl(),"afterLogout");

        // Default unauthorized handler
        bean.put(unauthorizedUrl(), "unauthorized");

        // Anything from the bypass list is ok
        for (String s:bypassUrlList())
        {
            bean.put(s,"anon");
        }

        // Everything else caught by the auth filter
        bean.put("/**","auth, "+sslConfigEntry());

        return bean;
    }

    /**
     * Sets up the shiro filter
     * @return
     */
    @Bean
    public Filter shiroFilter() {
        try {
            ShiroFilterFactoryBean factory = new ShiroFilterFactoryBean();
            factory.setFilterChainDefinitionMap(filterChainDefinitionMap());
            factory.setSuccessUrl(loginSuccessUrl());
            factory.setUnauthorizedUrl(unauthorizedUrl());

            // Manually add the Filters
            factory.getFilters().put("oauth", oAuthFilter());
            factory.getFilters().put("roles",roleFilter());
            factory.getFilters().put("logout", logoutFilter());
            factory.getFilters().put("ssl",sslFilter());
            factory.getFilters().put("auth",oauthPassThruAuthenticationFilter());
            factory.getFilters().put("anon", new AnonymousFilter());
            factory.getFilters().put("casFailure", oauthFailureFilter());
            factory.getFilters().put("afterLogout", logoutSuccessfulFilter());
            factory.getFilters().put("unauthorized", unauthorizedFilter());

            LOG.debug("Shiro Filters are: {}", factory.getFilters());

            factory.setSecurityManager(securityManager());

            return  (Filter)factory.getObject();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }



    /**
     * Sets up the ZuulAuthenticationFilter - used to force unauthenticated users to Zuul
     * @return
     */
    @Bean
    public OAuthPassThruAuthenticationFilter oauthPassThruAuthenticationFilter()
    {
        OAuthPassThruAuthenticationFilter bean = new OAuthPassThruAuthenticationFilter();
        String successUrl = loginSuccessUrl();
        if (successUrl!=null) {
            bean.setSuccessUrl(successUrl);
        }
        return bean;
    }

    /**
     * Sets up the ssl filter - forces users to ssl
     * @return
     */
    @Bean
    public SslFilter sslFilter()
    {
        SslFilter bean = new SslFilter();
        return bean;
    }

    /**
     * Sets up the logout filter (implements single log-out)
     * @return
     */
    @Bean
    public OAuthLogoutFilter logoutFilter()
    {
        OAuthLogoutFilter bean = new OAuthLogoutFilter();
        bean.setRedirectUrl(afterLogoutUrl());
        bean.setAfterLogoutEndpoint(afterLogoutUrl());
        bean.setOauthLogoutEndpoint(null); // TBD
        return bean;
    }

    /**
     * Sets up the security manager - hooks in the right subject factory and realms
     * @return
     */
    @Bean
    public SecurityManager securityManager()
    {
        DefaultWebSecurityManager bean = new DefaultWebSecurityManager();
        bean.setSubjectFactory(oAuthSubjectFactory());
        bean.setRealm(oauthRealm());
        bean.setCacheManager(new MemoryConstrainedCacheManager());
        return bean;
    }

    /**
     * Adds the lifecyclebeanpostprocessor - only used by AOP security annotations
     * @return
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor()
    {
        LifecycleBeanPostProcessor bean = new LifecycleBeanPostProcessor();
        return bean;
    }

    /**
     * Sets up the CasFilter that listens for returning Zuul tickets
     * @return
     */
    @Bean
    public OAuthFilter oAuthFilter()
    {
        OAuthFilter bean = new OAuthFilter();
        bean.setFailureUrl(failureUrl());
        return bean;
    }

    /**
     * Sets up the ZuulRealm that can process CAS tickets
     * @return
     */
    @Bean
    public OAuthRealm oauthRealm()
    {
        OAuthRealm bean = new OAuthRealm();

        return bean;
    }

    /**
     * A factory for creating subjects from cas tickets
     * @return
     */
    @Bean
    public OAuthSubjectFactory oAuthSubjectFactory()
    {
        OAuthSubjectFactory bean = new OAuthSubjectFactory();
        return bean;
    }

    /**
     * Filter for checking if a user has a role - needed to force redirect to ZUUL on a Shiro role-check
     * (as opposed to an 'authenticated' check)
     * @return
     */
    @Bean
    public RolesAuthorizationFilter roleFilter()
    {
        OAuthRolesAuthorizationFilter bean = new OAuthRolesAuthorizationFilter();
        return bean;
    }

    /**
     * Handles serving default output on cas failure
     * @return
     */
    @Bean
    public ShiroSimpleOutputFilter oauthFailureFilter()
    {
        ShiroSimpleOutputFilter bean = new ShiroSimpleOutputFilter();

        StringBuilder sb = new StringBuilder();
        sb.append("There was an error trying to logout.  <a href=\"");
        sb.append(logoutUrl());
        sb.append("\">Try logging out and back in</a>");

        bean.setContent(sb.toString());
        return bean;
    }

    /**
     * Handles serving default output on successful logout
     * @return
     */
    @Bean
    public ShiroSimpleOutputFilter logoutSuccessfulFilter()
    {
        ShiroSimpleOutputFilter bean = new ShiroSimpleOutputFilter();

        StringBuilder sb = new StringBuilder();
        sb.append("You have been logged out.  <a href=\"");
        sb.append(loginSuccessUrl());
        sb.append("\">Log back in.</a>");

        bean.setContent(sb.toString());
        return bean;
    }

    /**
     * Handles serving default output on insufficient roles for the requested page
     * @return
     */
    @Bean
    public ShiroSimpleOutputFilter unauthorizedFilter()
    {
        ShiroSimpleOutputFilter bean = new ShiroSimpleOutputFilter();

        StringBuilder sb = new StringBuilder();
        sb.append("You are not authorized to see this page");
        bean.setContent(sb.toString());
        return bean;
    }

    @Bean
    public GoogleOAuthService googleOAuthService()
    {
        GoogleOAuthService bean = new GoogleOAuthService();

        bean.setAuthenticationEndpoint("https://accounts.google.com/o/oauth2/auth");
        bean.setTokenExchangeEndpoint("https://accounts.google.com/o/oauth2/token");
        bean.setIdTokenInfoEndpoint("https://www.googleapis.com/oauth2/v1/tokeninfo?id_token={token}");
        bean.setAccessTokenInfoEndpoint("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token={token}");

        return bean;
    }

}

