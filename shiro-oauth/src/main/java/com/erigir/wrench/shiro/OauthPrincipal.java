package com.erigir.wrench.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class for shared functionality between various OAuthPrincipal providers.
 * <p>
 * Note that this class holds a set of roles and permissions - while this
 * isn't exactly how Shiro sees the use, I put them in here because:
 * A) I have to put them somewhere to allow you to modify them since they don't come from the oauth provider
 * B) Shiro doesn't have the ability to enumerate them out of the AuthorizationInfo class, just check existence
 * <p>
 * B is the main reason - sometimes I need to enumerate, and Shiro doesnt have it (at least in version 1.2.3)
 * <p>
 * Created by chrweiss on 5/29/15.
 */
public class OauthPrincipal {
  private Map<String, Object> otherData = new TreeMap<>();
  private String oauthProviderName;
  private Set<String> roles = new TreeSet<>();
  private Set<String> permissions = new TreeSet<>();

  /**
   * Helper method to extract role list from oauth principals in current session
   *
   * @return Set of strings containing all roles
   */
  public static Set<String> oauthRoles() {
    Set<String> rval = new TreeSet<>();
    for (OauthPrincipal p : oauthPrincipals()) {
      rval.addAll(p.getRoles());
    }
    return rval;
  }

  /**
   * Helper method to extract role list from oauth principals in current session
   *
   * @return Set of strings containing all permissions
   */
  public static Set<String> oauthPermissions() {
    Set<String> rval = new TreeSet<>();
    for (OauthPrincipal p : oauthPrincipals()) {
      rval.addAll(p.getPermissions());
    }
    return rval;
  }

  public static OauthPrincipal firstOauthPrincipal() {
    Collection<OauthPrincipal> c = oauthPrincipals();
    return (c.isEmpty()) ? null : c.iterator().next();
  }

  public static Collection<OauthPrincipal> oauthPrincipals() {
    Subject subject = SecurityUtils.getSubject();
    return (subject == null) ? Collections.EMPTY_LIST : subject.getPrincipals().byType(OauthPrincipal.class);
  }

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
