package com.erigir.wrench;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
  public static String envOrSysProperty(String propName, Object defaultVal) {
    Objects.requireNonNull(propName);
    String rval = System.getenv(propName);
    rval = (rval == null) ? System.getProperty(propName) : rval;
    rval = (rval == null && defaultVal!=null) ? String.valueOf(defaultVal) : rval;

    return rval;
  }

  /**
   * Searches system properties, environment, and default value in that order for the given property
   * @param propName String containing the property to find
   * @param defaultVal String containing the default value to use if not found anywhere
   * @return String containing the result
   */
  public static String sysOrEnvProperty(String propName, Object defaultVal) {
    Objects.requireNonNull(propName);
    String rval = System.getProperty(propName);
    rval = (rval == null) ? System.getenv(propName) : rval;
    rval = (rval == null && defaultVal!=null) ? String.valueOf(defaultVal) : rval;

    return rval;
  }

  /**
   * Searches environment, system properties, and default value in that order for the given property
   * @param propName String containing the property to find
   * @param defaultVal String containing the default value to use if not found anywhere
   * @param clazz Class containing the class to convert the resulting string to - must have a string only constructor
   * @return String containing the result
   */
  public static <T> T envOrSysProperty(String propName, Object defaultVal, Class<T> clazz) {
    String value = envOrSysProperty(propName, defaultVal);
    T rval = (value==null)?null:castUsingStringConstructor(value, clazz);
    return rval;
  }

  /**
   * Searches system properties, environment, and default value in that order for the given property
   * @param propName String containing the property to find
   * @param defaultVal String containing the default value to use if not found anywhere
   * @param clazz Class containing the class to convert the resulting string to - must have a string only constructor
   * @return String containing the result
   */
  public static <T> T sysOrEnvProperty(String propName, Object defaultVal, Class<T> clazz) {
    String value = sysOrEnvProperty(propName, defaultVal);
    T rval = (value==null)?null:castUsingStringConstructor(value, clazz);
    return rval;
  }

  /**
   * Given a string and a class with a string-only constructor, converts the string to that object
   * @param value String to convert
   * @param clazz Class to convert the string to
   * @param <T> Type to convert the string to
   * @return T Instance from that string
   * @throws IllegalArgumentException if the class doesn't have a string only constructor
   */
  public static <T> T castUsingStringConstructor(String value, Class<T> clazz) {
    Objects.requireNonNull(value);
    Objects.requireNonNull(clazz);
    try {
      T rval = null;
      if (value != null) {
        Constructor<T> constructor = clazz.getConstructor(String.class);
        rval = constructor.newInstance(value);
      }
      return rval;
    }
    catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e)
    {
      throw new IllegalArgumentException("Cannot cast string to "+clazz, e);
    }
  }


}
