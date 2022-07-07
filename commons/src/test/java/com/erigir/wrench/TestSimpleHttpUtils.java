package com.erigir.wrench;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by cweiss1271 on 3/9/16.
 */
public class TestSimpleHttpUtils {

  @Test
  public void testSimpleQuery() {
    SimpleHttpUtils.HttpTx tx = SimpleHttpUtils.quietFetchUrlDetails("https://www.google.com", 5000, 3);
    assertTrue(tx.getStatus() < 300);
  }

  @Test
  @Disabled
  public void testSimpleQuery2() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    SimpleStreamHttpUtils.StreamHttpTx tx = SimpleStreamHttpUtils.http(
        new SimpleStreamHttpUtils.SimpleStreamHttpRequest()
            .withUrl("https://www.google.com")
            .withConnectTimeout(5000)
            .withTries(3)
            .withDestination(baos)
    );

    String body = new String(baos.toByteArray());

    assertTrue(tx.getStatus() < 300);
  }

  @Test
  @Disabled
  public void testSimplePost2() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //ByteArrayInputStream input = new ByteArrayInputStream("THIS IS A TEST".getBytes());
    SimpleStreamHttpUtils.StreamHttpTx tx = SimpleStreamHttpUtils.http(
        new SimpleStreamHttpUtils.SimpleStreamHttpRequest()
            .withUrl("http://httpbin.org/post")
            .withMethod("POST")
            .withConnectTimeout(5000)
            .withTries(3)
            .withDestination(baos)
            .withSource("TEST2".getBytes())
        //.withSource(input)
    );

    String body = new String(baos.toByteArray());

    assertTrue(tx.getStatus() < 300);
  }

  @Test
  @Disabled
  public void testSimplePost() {
    byte[] postData = "This is a test".getBytes();
    Map<String, String> headers = new TreeMap<>();
    headers.put("Content-Type", "text/plain");


    String url = "https://test.server.com/v1/info/server";

    AllowSelfSignedHttps.allowSelfSignedHttpsCertificates();
    SimpleHttpUtils.HttpTx tx = SimpleHttpUtils.postDataToURL(url, headers, postData);
    assertTrue(tx.getStatus() < 300);
  }


}
