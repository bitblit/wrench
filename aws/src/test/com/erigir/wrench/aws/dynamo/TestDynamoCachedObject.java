package com.erigir.wrench.aws.dynamo;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
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
public class TestDynamoCachedObject {

  @Test
  @Ignore
  public void testDynamoCachedObject()
      throws Exception {
    Map<String, String> test = new TreeMap<>();
    test.put("a", "b");
    test.put("c", "d");

    AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
    AmazonDynamoDB dynamoDB = new AmazonDynamoDBClient(awsCredentialsProvider);
    DynamoDB dynamoDBV2 = new DynamoDB(dynamoDB);
    Table sharedCacheObjects = dynamoDBV2.getTable("shared-cached-objects");

    AWSCachedObject<Map<String, String>> doc = new DynamoCachedObject<Map<String, String>>(sharedCacheObjects, "test", "test", new TypeReference<Map<String, String>>() {
    });

    Map<String, String> toStart = doc.value();
    assertNull(toStart);

    doc.value(test);

    AWSCachedObject<Map<String, String>> doc2 = new DynamoCachedObject<Map<String, String>>(sharedCacheObjects, "test", "test", new TypeReference<Map<String, String>>() {
    });

    Map<String, String> afterSave = doc2.value();

    assertEquals(afterSave.get("a"), test.get("a"));

    doc2.value(null);

    AWSCachedObject<Map<String, String>> doc3 = new DynamoCachedObject<Map<String, String>>(sharedCacheObjects, "test", "test", new TypeReference<Map<String, String>>() {
    });
    Map<String, String> afterDelete = doc3.value();

    assertNull(afterDelete);


  }
}
