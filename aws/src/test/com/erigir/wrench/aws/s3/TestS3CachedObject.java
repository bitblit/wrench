package com.erigir.wrench.aws.s3;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.erigir.wrench.aws.wrench.AWSCachedObject;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by cweiss1271 on 3/1/16.
 */
public class TestS3CachedObject {

  @Test
  @Ignore
  public void testDynamoCachedObject()
      throws Exception {
    Map<String, String> test = new TreeMap<>();
    test.put("a", "b");
    test.put("c", "d");

    AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
    AmazonS3Client s3 = new AmazonS3Client(awsCredentialsProvider);
    String bucketName = "somebucket";
    String prefix = "dynamoCache";

    AWSCachedObject<Map<String, String>> doc = new S3CachedObject<Map<String, String>>(s3, bucketName, prefix, "test", "test", new TypeReference<Map<String, String>>() {
    });

    Map<String, String> toStart = doc.value();
    assertNull(toStart);

    doc.value(test);

    AWSCachedObject<Map<String, String>> doc2 = new S3CachedObject<Map<String, String>>(s3, bucketName, prefix, "test", "test", new TypeReference<Map<String, String>>() {
    });

    Map<String, String> afterSave = doc2.value();

    assertEquals(afterSave.get("a"), test.get("a"));

    doc2.value(null);

    AWSCachedObject<Map<String, String>> doc3 = new S3CachedObject<Map<String, String>>(s3, bucketName, prefix, "test", "test", new TypeReference<Map<String, String>>() {
    });
    Map<String, String> afterDelete = doc3.value();

    assertNull(afterDelete);


  }
}
