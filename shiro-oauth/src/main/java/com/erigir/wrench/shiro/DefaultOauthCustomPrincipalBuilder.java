package com.erigir.wrench.shiro;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * The default system just grants the default roles and privs to all
 * oauth authenticated users - modify this to handle your own cases.  You
 * can use this class if you want EVERY person who gets authenticated to
 * have the same roles and privileges (or if you only care about authentication)
 * <p>
 * Created by chrweiss on 5/29/15.
 */
public class DefaultOauthCustomPrincipalBuilder implements OauthCustomPrincipalBuilder {

  private Set<String> defaultRoles = new TreeSet<>();
  private Set<String> defaultPermissions = new TreeSet<>();

  public void updatePrincipal(OauthPrincipal data) {
    Objects.requireNonNull(data, "The Oauth principal object must be non-null");
    Objects.requireNonNull(defaultRoles, "The defaultRoles object must be non-null");
    Objects.requireNonNull(defaultPermissions, "The defaultPermissions object must be non-null");

    data.setPermissions(defaultPermissions);
    data.setRoles(defaultRoles);
  }

  public Set<String> getDefaultRoles() {
    return defaultRoles;
  }

  public void setDefaultRoles(Set<String> defaultRoles) {
    this.defaultRoles = defaultRoles;
  }

  public Set<String> getDefaultPermissions() {
    return defaultPermissions;
  }

  public void setDefaultPermissions(Set<String> defaultPermissions) {
    this.defaultPermissions = defaultPermissions;
  }

}
