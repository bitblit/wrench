package com.erigir.wrench.aws.cloudfront.logparser.handler;

import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogEntry;
import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogField;
import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogHandler;

import java.util.regex.Pattern;

/**
 * Created by chrweiss on 3/16/15.
 */
public class FieldRegexFilter implements CloudFrontAccessLogHandler {

  private CloudFrontAccessLogField field;
  private Pattern regex;

  public FieldRegexFilter() {
  }

  public FieldRegexFilter(CloudFrontAccessLogField field, String regex) {

    this.field = field;
    this.regex = Pattern.compile(regex);
  }

  @Override
  public boolean handleCloudFrontAccessLogEntry(CloudFrontAccessLogEntry entry) {
    boolean rval = regex.matcher(entry.field(field)).matches();
    return rval;
  }

  public Pattern getRegex() {
    return regex;
  }

  public void setRegex(Pattern regex) {
    this.regex = regex;
  }
}
