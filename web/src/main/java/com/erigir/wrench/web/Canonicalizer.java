package com.erigir.wrench.web;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Simple class for making strings URL friendly
 * Created by chrweiss on 6/7/15.
 */
public class Canonicalizer {
  /**
   * I know, % isn't technically reserved but...
   */
  private static Set<Character> RFC_3986_RESERVED =
      new TreeSet<>(Arrays.asList('!', '*', '\'', '(', ')', ';', ':', '@', '&', '=', '+', '$', ',', '/', '?', '#', '[', ']', '%'));

  public static String canonicalize(String value) {
    if (value == null) {
      return null;
    }
    StringBuilder rval = new StringBuilder();
    for (char c : value.toLowerCase().toCharArray()) {
      if (Character.isWhitespace(c)) {
        rval.append("-");
      } else if (RFC_3986_RESERVED.contains(c)) {
        // Do nothing, strip
      } else {
        rval.append(c);
      }
    }
    return rval.toString();
  }


}
