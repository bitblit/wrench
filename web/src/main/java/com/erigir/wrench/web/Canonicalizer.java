package com.erigir.wrench.web;

/**
 * Deprecated - moved to commons since this is not really a web thing
 * Use the one in com.erigir.wrench instead - this is just a passthru
 * Created by chrweiss on 6/7/15.
 */
@Deprecated
public class Canonicalizer {
  public static String canonicalize(String value) {
    return com.erigir.wrench.Canonicalizer.canonicalize(value);
  }
}
