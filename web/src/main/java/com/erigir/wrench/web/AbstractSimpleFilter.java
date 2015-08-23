package com.erigir.wrench.web;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A class for simplifying the filter interface - handles the commonly unused methods (init, destroy)
 * and casts the req and resp to the basically always used http versions
 *
 * Also adds a helper function for matching the url to various patterns
 * And a helper function for pulling values that might be in either a header or parameter (headers preferred)
 *
 * @author cweiss
 */
public abstract class AbstractSimpleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) arg0;
        HttpServletResponse resp = (HttpServletResponse) arg1;

        innerFilter(request, resp, arg2);
    }

    public abstract void innerFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException;

    @Override
    public void destroy() {
        // Do nothing
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // Do nothing
    }

    public static boolean matchesAtLeastOne(Collection<Pattern> pattern, String value) {
        boolean rval = false;
        if (pattern != null && value != null) {
            for (Iterator<Pattern> i = pattern.iterator(); i.hasNext() && !rval; ) {
                rval = i.next().matcher(value).matches();
            }
        }
        return rval;
    }

    public String getHeaderOrParam(HttpServletRequest wrapped, String name) {
        String rval = wrapped.getHeader(name);
        if (rval == null) {
            rval = wrapped.getParameter(name);
        }
        return rval;
    }

}
