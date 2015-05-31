package com.erigir.wrench.shiro.provider;

import com.erigir.wrench.shiro.OauthPrincipal;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Holds all the providers in the system, and adds some functionality to make them
 * easy to find by name.  It auto-registers the ones already in the package, but others can be added
 * easily enough by calling "add"
 *
 * Created by chrweiss on 5/30/15.
 */
public class ProviderRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(ProviderRegistry.class);
    public static final String DYNAMIC_SERVICE_PROVIDER_NAME_KEY = "shiro-oauth-dynamic-service-provider-name";

    private Map<String,OauthProvider> providerMap = new TreeMap<>();

    public void addProvider(OauthProvider provider)
    {
        Objects.requireNonNull(provider,"The provider may not be null");
        LOG.info("Adding provider {}",provider.getName());
        providerMap.put(provider.getName(), provider);
    }

    public boolean isEmpty()
    {
        return (providerMap==null || providerMap.size()==0);
    }

    public OauthProvider singleProvider()
    {
        return (providerMap.size()==1)?providerMap.entrySet().iterator().next().getValue():null;
    }

    public OauthProvider getProviderByName(String name)
    {
        return (name==null)?null:providerMap.get(name);
    }

    public void storeProviderForSession(OauthProvider provider)
    {
        Objects.requireNonNull(provider,"Provider may not be null");
        SecurityUtils.getSubject().getSession().setAttribute(DYNAMIC_SERVICE_PROVIDER_NAME_KEY, provider.getName());
    }

    public OauthProvider fetchProviderForSession()
    {
        String name = (String)SecurityUtils.getSubject().getSession().getAttribute(DYNAMIC_SERVICE_PROVIDER_NAME_KEY);
        Objects.requireNonNull(name, "Error - no provider name in session (likely timeout)");
        return getProviderByName(name);
    }

    public Map<String,OauthProvider> getProviderMap()
    {
        return Collections.unmodifiableMap(providerMap);
    }

}
