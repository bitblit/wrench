package com.erigir.wrench.simpleincludes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Scans the input string for {prefix}{filename}{suffix}, and replaces
 * each occurence with the contents of {filename}
 * <p>
 * Yes, this works all in memory.  Thats on purpose - it is meant to be used in 2
 * cases - 1) when running dev environment locally (single user load) or 2) running in
 * batch as part of seedy getting files ready for deployment
 * <p>
 * Created by cweiss on 8/4/15.
 */
public class SimpleIncludesProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(SimpleIncludesProcessor.class);
  private SimpleIncludesSource source;
  private String markerPrefix;
  private String markerSuffix;

  public SimpleIncludesProcessor() {
  }

  public SimpleIncludesProcessor(SimpleIncludesSource source, String markerPrefix, String markerSuffix) {
    this.source = source;
    this.markerPrefix = markerPrefix;
    this.markerSuffix = markerSuffix;
  }

  public String processIncludes(String input) {
    Objects.requireNonNull(input);
    Objects.requireNonNull(markerPrefix);
    Objects.requireNonNull(markerSuffix);
    Objects.requireNonNull(source);

    StringBuilder sb = new StringBuilder();
    int curLoc = 0;
    int startIdx = input.indexOf(markerPrefix);
    while (startIdx != -1) {
      int endIdx = input.indexOf(markerSuffix, startIdx);
      if (endIdx == -1) {
        throw new IllegalStateException("Found opening prefix at " + startIdx + " but no closing suffix");
      }
      // Find the data in question
      String sourceName = input.substring(startIdx + markerPrefix.length(), endIdx);
      String data = source.findContent(sourceName);
      if (data == null) {
        LOG.warn("Input requires {} but item not found in source.  Using blank", sourceName);
        data = "";
      }

      sb.append(input.substring(curLoc, startIdx));
      sb.append(data);
      curLoc = endIdx + markerSuffix.length();
      startIdx = input.indexOf(markerPrefix, curLoc);
    }
    // Finally add the last chunk
    sb.append(input.substring(curLoc));
    return sb.toString();
  }

  public void setSource(SimpleIncludesSource source) {
    this.source = source;
  }

  public void setMarkerPrefix(String markerPrefix) {
    this.markerPrefix = markerPrefix;
  }

  public void setMarkerSuffix(String markerSuffix) {
    this.markerSuffix = markerSuffix;
  }
}
