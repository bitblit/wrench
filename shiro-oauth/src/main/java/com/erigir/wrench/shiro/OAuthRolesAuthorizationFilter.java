package com.erigir.wrench.shiro;

import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Not sure if i need this yet...
 */
public class OAuthRolesAuthorizationFilter extends RolesAuthorizationFilter {
    private static final Logger LOG = LoggerFactory.getLogger(OAuthRolesAuthorizationFilter.class);


}
