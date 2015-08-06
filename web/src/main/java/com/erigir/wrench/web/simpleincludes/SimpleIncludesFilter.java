package com.erigir.wrench.web.simpleincludes;

import com.erigir.wrench.simpleincludes.SimpleIncludesFileSource;
import com.erigir.wrench.simpleincludes.SimpleIncludesProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * Pretty much copied from http://tutorials.jenkov.com/java-servlets/gzip-servlet-filter.html
 *
 * Created by cweiss on 7/29/15.
 */
public class SimpleIncludesFilter implements javax.servlet.Filter {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleIncludesFilter.class);
    private SimpleIncludesProcessor simpleIncludesProcessor;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("Configuring SimpleIncludesFilter");
        // Check if the default ssi filter was requested
        String defaultFileIncludePath=filterConfig.getInitParameter("defaultFileIncludePath");
        String defaultIncludeMode=filterConfig.getInitParameter("defaultIncludeMode");

        if (defaultFileIncludePath!=null && "HTML".equalsIgnoreCase(defaultIncludeMode))
        {
            LOG.info("Env:{} Props:{}", System.getenv(), System.getProperties());
            LOG.info("Default implementation requested for SimpleIncludesFilter, path={}",defaultFileIncludePath);
            String transformedPath = defaultFileIncludePath;
            transformedPath = (transformedPath.startsWith("prop:"))?System.getProperty(transformedPath.substring(5)):transformedPath;
            transformedPath = (transformedPath.startsWith("env:"))?System.getProperty(transformedPath.substring(4)):transformedPath;
            if (!transformedPath.equals(defaultFileIncludePath))
            {
                LOG.info("Transformed path to {}",transformedPath);
            }

            File parent = new File(transformedPath);
            if (!parent.exists() || !parent.isDirectory())
            {
                throw new IllegalArgumentException(defaultFileIncludePath+" doesnt exist or isnt a directory");
            }
            LOG.info("Effective path is : {}", parent.getAbsoluteFile());
            SimpleIncludesFileSource source = new SimpleIncludesFileSource(parent);
            SimpleIncludesProcessor processor = new SimpleIncludesProcessor(source, "<!--SI:",":SI-->");
            this.simpleIncludesProcessor = processor;
        }
    }

    @Override
    public void destroy() {
    }

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest  = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        SimpleIncludesServletResponseWrapper responseWrapper =
                new SimpleIncludesServletResponseWrapper(httpResponse,simpleIncludesProcessor);
        chain.doFilter(request, responseWrapper);
        responseWrapper.close();
    }

    public void setSimpleIncludesProcessor(SimpleIncludesProcessor simpleIncludesProcessor) {
        this.simpleIncludesProcessor = simpleIncludesProcessor;
    }
}