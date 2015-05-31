package com.erigir.wrench.shiro;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class for shared functionality between various OAuthPrincipal providers.
 *
 * Note that this class holds a set of roles and permissions - while this
 * isn't exactly how Shiro sees the use, I put them in here because:
 * A) I have to put them somewhere to allow you to modify them since they don't come from the oauth provider
 * B) Shiro doesn't have the ability to enumerate them out of the AuthorizationInfo class, just check existence
 *
 * B is the main reason - sometimes I need to enumerate, and Shiro doesnt have it (at least in version 1.2.3)
 *
 * Created by chrweiss on 5/29/15.
 */
public class OauthPrincipal {
    private Map<String,Object> otherData = new TreeMap<>();
    private String oauthProviderName;
    private Set<String> roles = new TreeSet<>();
    private Set<String> permissions = new TreeSet<>();

    public Map<String, Object> getOtherData() {
        return otherData;
    }

    public void setOtherData(Map<String, Object> otherData) {
        this.otherData = otherData;
    }

    public String getOauthProviderName() {
        return oauthProviderName;
    }

    public void setOauthProviderName(String oauthProviderName) {
        this.oauthProviderName = oauthProviderName;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
}
