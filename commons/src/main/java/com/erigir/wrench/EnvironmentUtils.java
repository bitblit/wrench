package com.erigir.wrench;

import java.util.Objects;

/**
 * Some utilities for working with environmental variables
 */
public class EnvironmentUtils {

  /**
   * Searches environment, system properties, and default value in that order for the given property
   * @param propName String containing the property to find
   * @param defaultVal String containing the default value to use if not found anywhere
   * @return String containing the result
   */
  public static String envOrSysProperty(String propName, String defaultVal) {
    Objects.requireNonNull(propName);
    String rval = System.getenv(propName);
    rval = (rval == null) ? System.getProperty(propName) : rval;
    rval = (rval == null) ? defaultVal : rval;

    return rval;
  }

  /**
   * Searches system properties, environment, and default value in that order for the given property
   * @param propName String containing the property to find
   * @param defaultVal String containing the default value to use if not found anywhere
   * @return String containing the result
   */
  public static String sysOrEnvProperty(String propName, String defaultVal) {
    Objects.requireNonNull(propName);
    String rval = System.getProperty(propName);
    rval = (rval == null) ? System.getenv(propName) : rval;
    rval = (rval == null) ? defaultVal : rval;

    return rval;
  }

}
