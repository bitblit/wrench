package com.erigir.wrench.shiro;


import com.erigir.wrench.shiro.spring.OauthShiroContext;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Created by chrweiss on 2/14/15.
 */
public class BootstrapTestServer {


    public static void main(String[] args) throws Exception {
        // Force the https port
        System.setProperty("shiro.https.port", "8443");


        // This is the minimal tomcat instance we need for embedding
        Tomcat tomcat = new Tomcat();
        // set http listen port for the default connector we get out-of-the-box
        // (there's a lot more you can customize, see the javadoc)
        tomcat.setPort(8080);

        Connector httpsConnector = new Connector();
        httpsConnector.setPort(8443);
        httpsConnector.setSecure(true);
        httpsConnector.setScheme("https");
        httpsConnector.setAttribute("keystoreFile", new File("shiro-oauth/src/test/config/tomcat-ssl.keystore").getAbsolutePath());
        httpsConnector.setAttribute("keystorePass", "jetty8");
        httpsConnector.setAttribute("clientAuth", "false");
        httpsConnector.setAttribute("sslProtocol", "TLS");
        httpsConnector.setAttribute("SSLEnabled", true);

        tomcat.getService().addConnector(httpsConnector);

        // set up context,
        //  "" indicates the path of the ROOT context
        //  tmpdir is used as docbase because we are not serving any files in this example
        File base = new File(System.getProperty("java.io.tmpdir"));
        Context rootCtx = tomcat.addContext("/", base.getAbsolutePath()); // "/test"
        // Add the main page servlet to the context

        DumpServlet servlet = new DumpServlet();

        tomcat.addServlet(rootCtx, "DumpServlet", servlet);
        rootCtx.addServletMapping("/*", "DumpServlet");

        AnnotationConfigApplicationContext sCtx = new AnnotationConfigApplicationContext(OauthShiroContext.class);

        Filter shiro = (Filter) sCtx.getBean("shiroFilter");

        FilterDef shiroFilterDef = new FilterDef();
        shiroFilterDef.setFilterName("shiroFilter");
        shiroFilterDef.setFilter(shiro);

        rootCtx.addFilterDef(shiroFilterDef);

        FilterMap shiroFilterMap = new FilterMap();
        shiroFilterMap.setFilterName("shiroFilter");
        //shiroFilterMap.setDispatcher("/*");
        shiroFilterMap.addURLPattern("/*");
        rootCtx.addFilterMap(shiroFilterMap);


        // ..and we are good to go
        tomcat.start();
        tomcat.getServer().await();
    }

    static class DumpServlet implements Servlet {
        @Override
        public void init(ServletConfig servletConfig) throws ServletException {
            // Do nothing
        }

        @Override
        public ServletConfig getServletConfig() {
            return null;
        }

        @Override
        public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
            HttpServletRequest req = (HttpServletRequest) servletRequest;
            HttpServletResponse resp = (HttpServletResponse) servletResponse;
            PrintWriter pw = resp.getWriter();

            resp.setContentType("text/plain");
            pw.println("Hello to request to :" + req.getRequestURI());


            Subject subject = SecurityUtils.getSubject();

            pw.println("Subject is:" + subject);
            //pw.println("Roles are:" + ZuulShiroUtils.subjectRoles());
            //pw.println("Perms are:" + ZuulShiroUtils.subjectPermissions());

            pw.println("Has role XXYYZZ: " + subject.hasRole("XXYYZZ"));
            pw.println("Has role oauth-user: " + subject.hasRole("oauth-user"));

            pw.println("Has perm P:A:B " + subject.isPermitted("P:A:B"));
            pw.println("Has perm oauth:test " + subject.isPermitted("oauth:test"));
            pw.println("Has perm oauth:t2 " + subject.isPermitted("oauth:test"));

        }

        @Override
        public String getServletInfo() {
            return null;
        }

        @Override
        public void destroy() {
            // Do nothing
        }
    }
}