package com.erigir.wrench.simpleincludes;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by cweiss on 8/4/15.
 */
public class TestSimpleIncludesProcessor {
  private SimpleIncludesSource testSource;
  private String markerStart = "<!--SI:";
  private String markerEnd = ":SI-->";

  private String toParse = "This is a test <!--SI:file1:SI--> and 2, <!--SI:file2:SI--> and 4 <!--SI:file4:SI--> and trailer";

  @BeforeAll
  public void setup() {
    Map<String, String> data = new TreeMap<>();
    data.put("file1", "file1-contents here");
    data.put("file2", "file2-contents here");
    data.put("file3", "file3-contents here");

    testSource = new SimpleIncludesMapSource(data);
  }

  @Test
  public void testSimpleIncludesProcessor() {
    SimpleIncludesProcessor proc = new SimpleIncludesProcessor();
    proc.setSource(testSource);
    proc.setMarkerPrefix(markerStart);
    proc.setMarkerSuffix(markerEnd);

    String out = proc.processIncludes(toParse);
    assertNotNull(out);

  }


}
