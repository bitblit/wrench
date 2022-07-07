package com.erigir.wrench.aws.cloudfront.logparser;

import com.erigir.wrench.aws.cloudfront.logparser.handler.ChainHandler;
import com.erigir.wrench.aws.cloudfront.logparser.handler.FieldCounter;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by chrweiss on 3/16/15.
 */
public class TestCloudFrontAccessLogEntry {
  private static final Logger LOG = LoggerFactory.getLogger(TestCloudFrontAccessLogEntry.class);
  public static String TEST_ACCESS_ENTRY_1 = "2015-03-12\t14:33:29\tLAX3\t6096\t1.2.3.4\tGET\tdvx8hnr8jwtp3.cloudfront.net\t/index.html\t200\thttps://test.com/\tMozilla/5.0%2520(iPhone;%2520CPU%2520iPhone%2520OS%25208_1_3%2520like%2520Mac%2520OS%2520X)%2520AppleWebKit/600.1.4%2520(KHTML,%2520like%2520Gecko)%2520Version/8.0%2520Mobile/12B466%2520Safari/600.1.4\t-\t-\tRefreshHit\tJXhU8Xa_6xWIr9f1M0JXjZQAtLYgK4g5tg7kPgs_qYKFauirBSkF8Q==\ttest.com\thttps\t351\t0.313";

  @Test
  @Ignore
  public void testFromLine()
      throws Exception {
    CloudFrontAccessLogEntry entry = new CloudFrontAccessLogEntry(TEST_ACCESS_ENTRY_1);

    assertEquals(entry.field(CloudFrontAccessLogField.DATE), "2015-03-12");
    assertEquals(entry.field(CloudFrontAccessLogField.TIME), "14:33:29");
    assertEquals(entry.field(CloudFrontAccessLogField.EDGE_LOCATION), "LAX3");
    assertEquals(entry.field(CloudFrontAccessLogField.SC_BYTES), "6096");
    assertEquals(entry.field(CloudFrontAccessLogField.IP), "1.2.3.4");
    assertEquals(entry.field(CloudFrontAccessLogField.METHOD), "GET");
    assertEquals(entry.field(CloudFrontAccessLogField.CS_HOST), "dvx8hnr8jwtp3.cloudfront.net");
    assertEquals(entry.field(CloudFrontAccessLogField.URI_STEM), "/index.html");
    assertEquals(entry.field(CloudFrontAccessLogField.STATUS), "200");
    assertEquals(entry.field(CloudFrontAccessLogField.REFERER), "https://test.com/");
    assertEquals(entry.field(CloudFrontAccessLogField.USER_AGENT), "Mozilla/5.0%2520(iPhone;%2520CPU%2520iPhone%2520OS%25208_1_3%2520like%2520Mac%2520OS%2520X)%2520AppleWebKit/600.1.4%2520(KHTML,%2520like%2520Gecko)%2520Version/8.0%2520Mobile/12B466%2520Safari/600.1.4");
    assertEquals(entry.field(CloudFrontAccessLogField.URI_QUERY), "-");
    assertEquals(entry.field(CloudFrontAccessLogField.COOKIE), "-");

    assertEquals(entry.field(CloudFrontAccessLogField.EDGE_RESULT_TYPE), "RefreshHit");
    assertEquals(entry.field(CloudFrontAccessLogField.EDGE_REQUEST_ID), "JXhU8Xa_6xWIr9f1M0JXjZQAtLYgK4g5tg7kPgs_qYKFauirBSkF8Q==");
    assertEquals(entry.field(CloudFrontAccessLogField.HOST_HEADER), "test.com");
    assertEquals(entry.field(CloudFrontAccessLogField.PROTOCOL), "https");
    assertEquals(entry.field(CloudFrontAccessLogField.CS_BYTES), "351");
    assertEquals(entry.field(CloudFrontAccessLogField.TIME_TAKEN), "0.313");


  }

  @Test
  public void deleteMe()
      throws Exception {
    ChainHandler handler = new ChainHandler();
    //handler.addHandler(new RawRegexFilter("."));

    //handler.addHandler(new DateFilter("2015-03-14 00:00:00","2015-03-15 00:00:00"));

    FieldCounter fc = new FieldCounter(CloudFrontAccessLogField.USER_AGENT_DECODED);
    handler.addHandler(fc);

    //handler.addHandler(new SimplePrintWriterHandler(new PrintWriter(new FileWriter("test.txt"))));

    new CloudFrontAccessLogParser().processLogs(new File("/Users/chrweiss/testgait/gait-website"), handler);

    PrintWriter pw = new PrintWriter(new FileWriter("test.txt"));

    for (Map.Entry<String, Integer> e : fc.getCounts().entrySet()) {
      pw.println(e.getKey() + " = " + e.getValue() + "\n");
    }

    pw.close();


    //new FileWriter("test.txt").write("Counts: \n\n"+fc.getCounts().toString());


    //LOG.info("Got: \n{}",fc.getCounts());


  }

}
