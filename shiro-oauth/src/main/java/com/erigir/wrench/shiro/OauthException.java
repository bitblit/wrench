package com.erigir.wrench.shiro;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by chrweiss on 5/12/15.
 */
public class OauthException extends RuntimeException {
  private Map<String, Object> otherInformation = new TreeMap<>();
  private int statusCode;

  public OauthException() {
  }

  public OauthException(String message) {
    super(message);
  }

  public OauthException(String message, Throwable cause) {
    super(message, cause);
  }

  public OauthException(Throwable cause) {
    super(cause);
  }

  public OauthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public Map<String, Object> getOtherInformation() {
    return otherInformation;
  }

  public void setOtherInformation(Map<String, Object> otherInformation) {
    this.otherInformation = otherInformation;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }
}
