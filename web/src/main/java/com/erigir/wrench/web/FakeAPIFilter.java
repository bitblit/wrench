package com.erigir.wrench.web;

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
import java.io.InputStream;
import java.util.Collections;

/**
 * Pretty much just sets the content type to application/json and turns on
 * CORS for everything to make it easy to fake an API from a static set of files
 * <p>
 * <p>
 * Created by cweiss on 1/7/16.
 */
public class FakeAPIFilter implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(FakeAPIFilter.class);

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    LOG.info("Configuring FakeAPIFilter");
  }

  @Override
  public void destroy() {
  }

  public void doFilter(ServletRequest request,
                       ServletResponse response,
                       FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    LOG.info("Fake api processing httpRequest : \n\n{}\n\n", describeRequest(httpRequest));

    httpResponse.setContentType("application/json");
    httpResponse.setHeader("Access-Control-Allow-Origin", "*");
    httpResponse.setHeader("Access-Control-Allow-Methods", "*");
    httpResponse.setHeader("Access-Control-Max-Age", "6000");
    httpResponse.setHeader("Access-Control-Allow-Headers", "accept, content-type, x-timestamp, x-token");//"*");

    chain.doFilter(request, httpResponse);
  }

  private String describeRequest(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();

    try {
      sb.append(request.getScheme()).append("://")
          .append(request.getServerName()).append(":")
          .append(request.getServerPort());
      String contextPath = request.getContextPath();
      if (contextPath != null && contextPath.length() > 0) {
        sb.append(contextPath);
      }
      sb.append(request.getRequestURI());

      String queryString = request.getQueryString();
      if (queryString != null && queryString.length() > 0) {
        sb.append("?").append(queryString);
      }
      sb.append("\n");
      sb.append("Method: ").append(request.getMethod()).append("\n");
      sb.append("Headers:\n");
      for (String h : Collections.list(request.getHeaderNames())) {
        sb.append(h).append(" = ").append(request.getHeader(h)).append("\n");
      }
      sb.append("Body:\n-----\n");
      InputStream is = request.getInputStream();
      int nextChar = is.read();
      while (nextChar != -1) {
        sb.append((char) nextChar);
        nextChar = is.read();
      }
      sb.append("\n-----\nEND BODY\n");
    } catch (Exception e) {
      sb.append("Error describing : ").append(e);
    }

    return sb.toString();
  }

}