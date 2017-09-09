package com.erigir.wrench.aws.cloudfront.logparser.handler;

import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogEntry;
import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogField;
import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogHandler;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by chrweiss on 3/16/15.
 */
public class FieldCounter implements CloudFrontAccessLogHandler {

  private CloudFrontAccessLogField field;
  private Map<String, Integer> counts = new TreeMap<>();

  public FieldCounter() {
  }

  public FieldCounter(CloudFrontAccessLogField field) {
    this.field = field;
  }

  @Override
  public boolean handleCloudFrontAccessLogEntry(CloudFrontAccessLogEntry entry) {
    String value = entry.field(field);
    Integer x = counts.get(value);

    int newVal = (x == null) ? 1 : x + 1;
    counts.put(value, newVal);

    return true;
  }

  public CloudFrontAccessLogField getField() {
    return field;
  }

  public void setField(CloudFrontAccessLogField field) {
    this.field = field;
  }

  public Map<String, Integer> getCounts() {
    return Collections.unmodifiableMap(counts);
  }
}
