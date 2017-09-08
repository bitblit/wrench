package com.erigir.wrench.shiro;

import com.erigir.wrench.shiro.provider.OauthProvider;
import com.erigir.wrench.shiro.provider.ProviderRegistry;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * This realm implementation acts as a OAuth client to a OAuth server for authentication and basic authorization.
 * <p>
 * This realm functions by inspecting a submitted {@link OauthToken OAuthToken} (which essentially
 * wraps a OAuth service ticket) and validates it against the OAuth server using a configured OAuth Validator
 * <p>
 * It is based on
 * {@link AuthorizingRealm AuthorizingRealm} for both authentication and authorization. User id and attributes are retrieved from the OAuth
 * ticket validation response during authentication phase. Roles and permissions are computed during authorization phase (according
 * to the attributes previously retrieved).
 */
public class OauthRealm extends AuthorizingRealm {

  // default name of the OAuth attribute for remember me authentication
  //public static final String DEFAULT_REMEMBER_ME_ATTRIBUTE_NAME = "longTermAuthenticationRequestTokenUsed";
  public static final String DEFAULT_VALIDATION_PROTOCOL = "CAS";

  private static Logger LOG = LoggerFactory.getLogger(OauthRealm.class);

  private ProviderRegistry providerRegistry;
  // this is the CAS service url of the application (example : http://host:port/mycontextpath/shiro-cas)
  private String oauthService;

  /* CAS protocol to use for ticket validation : CAS (default) or SAML :
     - CAS protocol can be used with CAS server version < 3.1 : in this case, no user attributes can be retrieved from the CAS ticket validation response (except if there are some customizations on CAS server side)
     - SAML protocol can be used with CAS server version >= 3.1 : in this case, user attributes can be extracted from the CAS ticket validation response
  */
  private String validationProtocol = DEFAULT_VALIDATION_PROTOCOL;

  // default name of the CAS attribute for remember me authentication (CAS 3.4.10+)
  //private String rememberMeAttributeName = DEFAULT_REMEMBER_ME_ATTRIBUTE_NAME;

  private OauthCustomPrincipalBuilder oauthCustomPrincipalBuilder;


  public OauthRealm() {
    setAuthenticationTokenClass(OauthToken.class);
  }

  @Override
  protected void onInit() {
    super.onInit();
  }

  /**
   * Authenticates a user and retrieves its information.
   *
   * @param token the authentication token
   * @throws AuthenticationException if there is an error during authentication.
   */
  @Override
  @SuppressWarnings("unchecked")
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    OauthToken oauthToken = (OauthToken) token;
    if (oauthToken == null || !StringUtils.hasText(oauthToken.getCredentials())) {
      return null;
    }

    try {
      // contact OAuth server to validate service ticket
      String redirURL = (String) SecurityUtils.getSubject().getSession().getAttribute(DynamicOauthLoginFilter.DYNAMIC_RETURN_URL_KEY);
      // Choose which oauth provider to use
      OauthProvider provider = providerRegistry.fetchProviderForSession();
      // Exchange the token for a access token (proves authentication as a side effect)
      OauthPrincipal reply = provider.validate(oauthToken, redirURL);
      // Fetch any other user data that was requested
      provider.fetchUserData(reply);
      // Set the provider name, mainly for debugging
      reply.setOauthProviderName(provider.getName());
      // If we are here, add roles/privs
      oauthCustomPrincipalBuilder.updatePrincipal(reply);


      LOG.debug("Validate ticket : {} in OAuth server : {} to retrieve accesstoken : {}", new Object[] {
          oauthToken, provider.getClass(), reply});

            /*
            String rememberMeAttributeName = getRememberMeAttributeName();
            String rememberMeStringValue = (String)attributes.get(rememberMeAttributeName);
            boolean isRemembered = rememberMeStringValue != null && Boolean.parseBoolean(rememberMeStringValue);
            if (isRemembered) {
                oauthToken.setRememberMe(true);
            }
            */

      // create simple authentication info
      List<Object> principals = CollectionUtils.asList((Object) reply);
      PrincipalCollection principalCollection = new SimplePrincipalCollection(principals, getName());
      return new SimpleAuthenticationInfo(principalCollection, oauthToken.getCredentials());
    } catch (OauthException e) {
      throw new OauthAuthenticationException("Unable to validate token [" + oauthToken + "]", e);
    }
  }

  /**
   * Retrieves the AuthorizationInfo for the given principals (the Oauth previously authenticated user : id + attributes).
   *
   * @param principals the primary identifying principals of the AuthorizationInfo that should be retrieved.
   * @return the AuthorizationInfo associated with this principals.
   */
  @Override
  @SuppressWarnings("unchecked")
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    // retrieve user information
    SimplePrincipalCollection principalCollection = (SimplePrincipalCollection) principals;

    Collection<OauthPrincipal> listPrincipals = principalCollection.byType(OauthPrincipal.class);
    OauthPrincipal principal = listPrincipals.iterator().next();
    // create simple authorization info
    SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();

    // Add roles to the authInfo
    for (String s : principal.getRoles()) {
      simpleAuthorizationInfo.addRole(s);
    }
    simpleAuthorizationInfo.addStringPermissions(principal.getPermissions());

    return simpleAuthorizationInfo;
  }

  public String getOauthService() {
    return oauthService;
  }

  public void setOauthService(String oauthService) {
    this.oauthService = oauthService;
  }

  public String getValidationProtocol() {
    return validationProtocol;
  }

  public void setValidationProtocol(String validationProtocol) {
    this.validationProtocol = validationProtocol;
  }

    /*
    public String getRememberMeAttributeName() {
        return rememberMeAttributeName;
    }

    public void setRememberMeAttributeName(String rememberMeAttributeName) {
        this.rememberMeAttributeName = rememberMeAttributeName;
    }
    */

  public OauthCustomPrincipalBuilder getOauthCustomPrincipalBuilder() {
    return oauthCustomPrincipalBuilder;
  }

  public void setOauthCustomPrincipalBuilder(OauthCustomPrincipalBuilder oauthCustomPrincipalBuilder) {
    this.oauthCustomPrincipalBuilder = oauthCustomPrincipalBuilder;
  }

  public void setProviderRegistry(ProviderRegistry providerRegistry) {
    this.providerRegistry = providerRegistry;
  }
}
