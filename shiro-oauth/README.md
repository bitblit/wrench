# Erigir Wrench Shiro OAuth Adapter

A set of classes to implement Shiro security using OAuth as your Authentication, with some
helpers to add Authorization as well 

Note that the way this is written right now is mainly to handle my particular use case, which is that I prefer to not
write my own authenticators, so I just throw the user to Google or Facebook.  I don't really use the access tokens that
are returned, so I don't store them or use them to post on anyones wall or anything.  You can, if you like, since the
access_token for both Google and Facebook providers are stored in the otherData field of the OauthPrincipal
(Try calling the static method OauthPrincipal.firstOauthPrincipal().getOtherData().get("access_token") if you want it)
 but using them is outside of the scope of this document.  Maybe try Spring Social or something.  An example call to 
 the Facebook graph api can be found inside the FacebookProvider class if you care.


This library is HEAVILY indebted to the shiro-cas library, after which it is extensively modeled.

# Adding shiro-oauth to your project

## Adding the maven dependency

```xml

<dependency>
    <groupId>com.erigir</groupId>
    <artifactId>wrench-shiro-oauth</artifactId>
    <version>(version number here)</version>
</dependency>
```

Maven central can point you to the most current production version

## Adding the context to spring

If you are using Java config, then you need to add the following to your config:

```java
@Import({OauthShiroContext.class}) // bring in zuul for authentication
```

If, on the other hand, you are using XML configuration, you would use:

```xml
<bean name="/OauthShiroContext" class="com.erigir.wrench.shiro.spring.OauthShiroContext" />
```

Important note : YOU MUST CONFIGURE AT LEAST ONE OAUTH PROVIDER (and if you configure more than one, a selector url)
otherwise the system will not start.

## Adding the Shiro filter to secure the application

If you are using Java config (Servlet 3.x approach) using Spring's WebApplicationInitializer, you'd add it like so:

```java
    EnumSet<DispatcherType> shiroDispatcherTypes = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ERROR, DispatcherType.INCLUDE);
    DelegatingFilterProxy securityDelegator = new DelegatingFilterProxy();
    securityDelegator.setTargetFilterLifecycle(true);
    FilterRegistration.Dynamic securityFilter = servletContext.addFilter("shiroFilter", securityDelegator);
    securityFilter.addMappingForUrlPatterns(shiroDispatcherTypes, true, "/*");
```

If you are using web.xml, it would look like this:

```xml
    <filter>
        <filter-name>shiroFilter</filter-name>
        <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
    </filter>
    <filter-mapping>
      <filter-name>shiroFilter</filter-name>
      <url-pattern>/*</url-pattern>
    </filter-mapping>
```

## Add a logout link

Somewhere on your pages (if you wish) add a link to `/logout` - this will allow log-out.

Important!  If you do this you'll probably want to setup an unsecured post-logout page (bean named "afterLogoutUrl"), 
otherwise the url will be secured, which will cause the browser to automatically redirect back to the Oauth server
 which will likely immediately log the user in again (can be really frustrating to debug!)

# Things you can (easily) customize

You could override a LOT of this, but here are some simple things you might want to override.  In each case,
you override the value by providing a bean with that name and type explicitly set.

```
 Name: bypassUrlList
 Type: List<String>
 Default: "/favicon.ico","/static/**","/health-check",providerSelectorUrl()
 Description: The set of urls that AREN'T secured by Shiro
```

```
 Name: loginSuccessUrl
 Type: String
 Default: /index.html
 Description: Where the user should be redirected back to after a successful login
```

```
 Name: unauthorizedUrl
 Type: String
 Default: /unauthorized
 Description: Where to redirect if the user lacks a required role.
```

```
 Name: afterLogoutUrl
 Type: String
 Default: /logged-out
 Description: Where to redirect after successful logout.  If changed, this URL MUST be in the bypassUrlList
```

```
 Name: sslPort
 Type: String
 Default: If there is a 'shiro.https.port' environmental variable, uses that.  Otherwise, 443
 Description: The port the user should be redirected to for HTTPS.  The env variable is to make local testing easy
```

```
 Name: oauthCustomPrincipalBuilder
 Type: OauthCustomPrincipalBuilder
 Default: An instance of DefaultOauthCustomPrincipalBuilder which gives all users the oauth-user role and oauth:* perms
 Description: Extend the default one if you wish to use permissions or change the way that roles are named or filtered
```

```
 Name: facebookProvider
 Type: FacebookProvider
 Default: Not created by default
 Description: Set the client id and secret (and optionally the scope) to enable facebook login
```

```
 Name: googleProvider
 Type: GoogleProvider
 Default: Not created by default
 Description: Set the client id and secret (and optionally the scope) to enable google login
```

```
 Name: extraShiroUrlMappings
 Type: LinkedHashMap<String,String>
 Default: An empty map
 Description: Allows you to add extra mappings, especially for roles and permissions filters if you wish for
 shiro to handle the filtering at the URL level.  Map is URL to definition. For example, for a role mapping:

 /admin.zul -> roles[ROLE_ADMIN]

 For a permissions mapping:

 /admin.zul -> perms[admin:view]

 all mappings in this set get the HTTPS filter added to them as well

```

# Changes

## Version 0.6
* Initial Release for this part of Wrench

# Future Development

* Need to test/verify around when this is used in a context other than the root context (/)


