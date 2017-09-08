package com.erigir.wrench.aws.cloudfront.logparser;

import com.erigir.wrench.QuietUtils;
import com.erigir.wrench.UTF8Encoder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by chrweiss on 3/16/15.
 */
public class CloudFrontAccessLogEntry {
  public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

  private Map<CloudFrontAccessLogField, String> fields = new TreeMap<>();
    /*private String dateString;
    private String raw;
    private String timeString;
    private String edgeLocation;
    private long scBytes;

    private String ip;
    private String method;
    private String csHost;
    private String uriStem;
    private String status;
    private String referer;
    private String userAgent;
    private String uriQuery;
    private String cookie;

    private String edgeResultType;
    private String edgeRequestId;
    private String hostHeader;
    private String protocol;
    private long csBytes;
    
    private BigDecimal timeTaken;*/

  private Map<String, Object> meta;

  public CloudFrontAccessLogEntry() {
    super();
  }

  public CloudFrontAccessLogEntry(String inLine) {
    super();
    populateFromLogLine(inLine);
  }

  public void populateFromLogLine(String inLine) {
    if (inLine == null) {
      throw new IllegalArgumentException("Line may not be null");
    }
    String[] broken = inLine.split("\t");
    if (broken.length != 19) {
      throw new IllegalArgumentException("Line requires 19 segments but found " + broken.length + " in " + inLine);
    }

    fields.put(CloudFrontAccessLogField.DATE, broken[0]);
    fields.put(CloudFrontAccessLogField.TIME, broken[1]);
    fields.put(CloudFrontAccessLogField.EDGE_LOCATION, broken[2]);
    fields.put(CloudFrontAccessLogField.SC_BYTES, broken[3]);

    fields.put(CloudFrontAccessLogField.IP, broken[4]);
    fields.put(CloudFrontAccessLogField.METHOD, broken[5]);
    fields.put(CloudFrontAccessLogField.CS_HOST, broken[6]);
    fields.put(CloudFrontAccessLogField.URI_STEM, broken[7]);
    fields.put(CloudFrontAccessLogField.STATUS, broken[8]);
    fields.put(CloudFrontAccessLogField.REFERER, broken[9]);
    fields.put(CloudFrontAccessLogField.USER_AGENT, broken[10]);
    fields.put(CloudFrontAccessLogField.URI_QUERY, broken[11]);
    fields.put(CloudFrontAccessLogField.COOKIE, broken[12]);

    fields.put(CloudFrontAccessLogField.EDGE_RESULT_TYPE, broken[13]);
    fields.put(CloudFrontAccessLogField.EDGE_REQUEST_ID, broken[14]);
    fields.put(CloudFrontAccessLogField.HOST_HEADER, broken[15]);
    fields.put(CloudFrontAccessLogField.PROTOCOL, broken[16]);
    fields.put(CloudFrontAccessLogField.CS_BYTES, broken[17]);

    fields.put(CloudFrontAccessLogField.TIME_TAKEN, broken[18]);

    // Special ones now
    fields.put(CloudFrontAccessLogField.RAW, inLine);

        /*
        try {
            String test = "http://test.com/a?q="+broken[10];
            URI dec = new URI(test);
            String p =dec.getRawQuery();
            int x=0;
        }
        catch (Exception e)
        {
            // do nothing
        }
        */
    fields.put(CloudFrontAccessLogField.USER_AGENT_DECODED, UTF8Encoder.decode(UTF8Encoder.decode(broken[10])));
  }

  public String field(CloudFrontAccessLogField field) {
    return fields.get(field);
  }

  //date time x-edge-location sc-bytes c-ip cs-method cs(Host) cs-uri-stem sc-status cs(Referer) cs(User-Agent) cs-uri-query cs(Cookie)
  //edge-result-type x-edge-request-id x-host-header cs-protocol cs-bytes time-taken

  public Date getDate() {
    return (field(CloudFrontAccessLogField.DATE) == null || field(CloudFrontAccessLogField.TIME) == null) ? null : QuietUtils.quietParse(field(CloudFrontAccessLogField.DATE) + " " + field(CloudFrontAccessLogField.TIME), DATE_FORMAT);
  }

  public long getCsBytesAsLong() {
    return new Long(field(CloudFrontAccessLogField.CS_BYTES));
  }

  public long getScBytesAsLong() {
    return new Long(field(CloudFrontAccessLogField.SC_BYTES));
  }

  public BigDecimal getDuration() {
    return new BigDecimal(field(CloudFrontAccessLogField.TIME_TAKEN));
  }

  public Map<String, Object> getMeta() {
    return meta;
  }

  public void setMeta(Map<String, Object> meta) {
    this.meta = meta;
  }

  public void addMeta(String key, Object value) {
    if (meta == null) {
      meta = new TreeMap<>();
    }
    meta.put(key, value);
  }
}
