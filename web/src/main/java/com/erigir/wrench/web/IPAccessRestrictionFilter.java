package com.erigir.wrench.web;

import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This filter restricts access to the set of URLs to which it is mapped, to a set of IP addresses
 *
 * Created by chrweiss on 12/19/14.
 */
public class IPAccessRestrictionFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(IPAccessRestrictionFilter.class);
    private List<String> ipHeadersToSearch = Collections.unmodifiableList(Arrays.asList("CLIENT_IP", "X-Forwarded-For", "REMOTE_ADDR"));
    private List<SubnetUtils.SubnetInfo> ipPatterns;
    private int rejectStatusCode = 403;
    private String rejectStatusText = "403: Forbidden";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)servletRequest;
        HttpServletResponse resp = (HttpServletResponse)servletResponse;
        boolean acceptedIP = isAcceptedRequest(req);

        if (acceptedIP)
        {
            LOG.trace("Request accepted - passing thru");
            filterChain.doFilter(servletRequest,servletResponse);
        }
        else
        {
            sendNotAccepted(resp);
        }
    }
    private void sendNotAccepted(HttpServletResponse resp)
            throws IOException
    {
        resp.setStatus(rejectStatusCode);
        resp.setContentType("text/html");
        resp.getWriter().println(rejectStatusText);
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    private String getMostLikelyRemoteAddress(final HttpServletRequest req) {
        if (null == req) {
            throw new IllegalArgumentException("Null request.");
        }
        String rval = null;
        if (ipHeadersToSearch!=null) {
            for (String header : ipHeadersToSearch) {
                rval = req.getHeader(header);
                if (null != rval) {
                    LOG.trace("Found remote ip address[{}] in header: {}", rval,
                            header);
                    break;
                } else {
                    LOG.trace("Remote ip address not found in header: {}", header);
                }
            }
        }
        if (rval == null) {
            rval = req.getRemoteAddr();
            LOG.trace("Falling back to request remote address method: {}", rval);
        }
        return rval;
    }

    private boolean isAcceptedRequest(final HttpServletRequest request) {
        boolean rval = false;

        if (ipPatterns!=null) {
            final String ipAddress = getMostLikelyRemoteAddress(request);
            LOG.debug("Testing {} against allowed IP ranges",ipAddress);

            for (Iterator<SubnetUtils.SubnetInfo> i = ipPatterns.iterator();i.hasNext() && !rval;)
            {
                SubnetUtils.SubnetInfo s = i.next();
                rval = (s.isInRange(ipAddress));
                if (rval)
                {
                    LOG.trace("Accepted match ip {} to {}", ipAddress, s.getCidrSignature());
                }
                else
                {
                    LOG.trace("No match of {} to {}",ipAddress,s.getCidrSignature());
                }
            }

        }
        else
        {
            LOG.warn("IPAccess restriction filter allowing all through since patterns are not set - likely misconfiguration");
        }

        return rval;
    }

    public void setIpPatterns(List<SubnetUtils.SubnetInfo> internalIpPatterns) {
        this.ipPatterns = internalIpPatterns;

        if (internalIpPatterns!=null)
        {
            for (SubnetUtils.SubnetInfo s:internalIpPatterns)
            {
                LOG.info("Internal subnet : {} from {} to {} ({} hosts)", new Object[]{s.getCidrSignature(), s.getLowAddress(), s.getHighAddress(), s.getAddressCount()});
            }
        }

    }

    public void setIpPatternsByString(List<String> ipPatternStrings)
    {
        if (ipPatternStrings==null)
        {
            ipPatterns = null;
        }
        else
        {
            ipPatterns = new LinkedList<>();
            for (String s:ipPatternStrings)
            {
                SubnetUtils su = new SubnetUtils(s);
                if (s.endsWith("/32"))
                {
                    su.setInclusiveHostCount(true);
                }
                ipPatterns.add(su.getInfo());
            }
        }
    }

    public void setIpHeadersToSearch(List<String> ipHeadersToSearch) {
        this.ipHeadersToSearch = ipHeadersToSearch;
    }

    public void setRejectStatusCode(int rejectStatusCode) {
        this.rejectStatusCode = rejectStatusCode;
    }

    public void setRejectStatusText(String rejectStatusText) {
        this.rejectStatusText = rejectStatusText;
    }
}
