package com.erigir.wrench.shiro;

/**
 * Classes implementing this interface basically are expected to
 * populate the roles and privileges section of the OauthPrincipal
 * object based on the systems needs  See also
 * DefaultOauthCustomPrincipalBuilder
 *
 * Created by chrweiss on 2/22/15.
 */
public interface OauthCustomPrincipalBuilder {
    void updatePrincipal(OauthPrincipal data);
}
