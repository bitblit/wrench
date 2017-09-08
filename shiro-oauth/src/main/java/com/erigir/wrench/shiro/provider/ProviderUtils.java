package com.erigir.wrench.shiro.provider;

import com.erigir.wrench.UTF8Encoder;
import com.erigir.wrench.shiro.OauthException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/**
 * This replicates a few pieces of Spring's RestTemplate, just because I didn't want to add
 * a general spring dependency
 * Created by chrweiss on 5/30/15.
 */
public class ProviderUtils {
  private static final Logger LOG = LoggerFactory.getLogger(ProviderUtils.class);

  /**
   * Basically does an HTTP GET on a URL and parses the body as JSON if its a 200, throws exception otherwise
   *
   * @param url          String containing the url to request
   * @param objectMapper an objectMapper to parse the returned JSON
   * @return Map containing the parsed json
   * @throws OauthException on io or other error
   */
  public static Map<String, Object> httpGetUrlParseJsonBody(String url, ObjectMapper objectMapper)
      throws OauthException {
    try {
      HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

      if (conn.getResponseCode() == 200) {
        return objectMapper.readValue(conn.getInputStream(), Map.class);
      } else {
        LOG.info("Bad response code:{}", conn.getResponseCode());
        OauthException oe = new OauthException();
        oe.setOtherInformation(objectMapper.readValue(conn.getErrorStream(), Map.class));
        oe.setStatusCode(conn.getResponseCode());
        throw oe;
      }

    } catch (Exception e) {
      LOG.warn("Error requesting item remotely", e);
      OauthException oe = new OauthException(e);
      throw oe;
    }

  }

  /**
   * Converts the body data into post format and does a post, parses the response as JSON.
   * If the order of the parameters matters to you, use a linked hash map
   *
   * @param bodyData     Map containing any fields you want in the body
   * @param url          String containing the url to request
   * @param objectMapper an objectMapper to parse the returned JSON
   * @return Map containing the parsed json
   * @throws OauthException on io or other error
   */
  public static Map<String, Object> httpPostUrlParseJsonBody(String url, Map<String, Object> bodyData, ObjectMapper objectMapper)
      throws OauthException {
    try {
      StringBuilder sb = new StringBuilder();

      for (Iterator<Map.Entry<String, Object>> i = bodyData.entrySet().iterator(); i.hasNext(); ) {
        Map.Entry<String, Object> e = i.next();
        sb.append(e.getKey()).append("=").append(UTF8Encoder.encode(String.valueOf(e.getValue())));
        if (i.hasNext()) {
          sb.append("&");
        }
      }
      byte[] postData = sb.toString().getBytes(StandardCharsets.UTF_8);

      HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
      conn.setDoOutput(true);
      conn.setInstanceFollowRedirects(false);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("charset", "utf-8");
      conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
      conn.setUseCaches(false);
      conn.getOutputStream().write(postData);

      if (conn.getResponseCode() == 200) {
        return objectMapper.readValue(conn.getInputStream(), Map.class);
      } else {
        LOG.info("Bad response code:{}", conn.getResponseCode());
        OauthException tve = new OauthException();
        tve.setOtherInformation(objectMapper.readValue(conn.getErrorStream(), Map.class));
        tve.setStatusCode(conn.getResponseCode());
        throw tve;
      }
    } catch (Exception e) {
      LOG.warn("Error performing PUT to {}", url, e);
      throw new OauthException(e);
    }
  }

  public static String defaultProviderRegistryName(Class input) {
    String rval = input.getSimpleName().toLowerCase();
    if (rval.endsWith("provider")) {
      rval = rval.substring(0, rval.length() - "provider".length());
    }
    return rval;
  }

}
