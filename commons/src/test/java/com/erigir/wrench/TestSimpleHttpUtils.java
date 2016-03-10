package com.erigir.wrench;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * Created by cweiss1271 on 3/9/16.
 */
public class TestSimpleHttpUtils {

    @Test
    @Ignore
    public void testSimpleQuery()
    {
        SimpleHttpUtils.HttpTx tx = SimpleHttpUtils.quietFetchUrlDetails("http://www.yahoo.com",5000,3);
        assertTrue(tx.getStatus()<300);
    }

    @Test
    @Ignore
    public void testSimplePost()
    {
        byte[] postData = "This is a test".getBytes();
        Map<String,String> headers = new TreeMap<>();
        headers.put("Content-Type","text/plain");


        String url = "https://test.server.com/v1/info/server";

        AllowSelfSignedHttps.allowSelfSignedHttpsCertificates();
        SimpleHttpUtils.HttpTx tx = SimpleHttpUtils.postDataToURL(url,headers,postData );
        assertTrue(tx.getStatus()<300);
    }


}
