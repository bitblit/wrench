package com.erigir.wrench.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.erigir.wrench.aws.wrench.AbstractAWSCachedObject;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * An implementation of cached object that uses S3 as its
 * backing object.
 * <p>
 * Note that for small objects often DynamoCachedObject will
 * be faster/more consistent in read/write times, but S3
 * can hold much bigger objects
 * <p>
 * Created by cweiss1271 on 2/29/16.
 */
public class S3CachedObject<T> extends AbstractAWSCachedObject<T> {
  private static final Logger LOG = LoggerFactory.getLogger(S3CachedObject.class);
  private AmazonS3 s3;
  private String bucket;
  private String prefix;
  private String applicationId;
  private String objectId;

  public S3CachedObject(AmazonS3 s3, String bucket, String prefix, String applicationId, String objectId, Class<T> clazz) {
    super(clazz);
    Objects.requireNonNull(s3);
    Objects.requireNonNull(bucket);
    Objects.requireNonNull(applicationId);
    Objects.requireNonNull(objectId);

    if (applicationId.startsWith("/") || objectId.startsWith("/") || objectId.endsWith("/")) {
      throw new IllegalArgumentException("Neither applicationId nor objectId may start with / and objectId may not end with it");
    }

    this.s3 = s3;
    this.bucket = bucket;
    this.prefix = StringUtils.trimToEmpty(prefix);
    this.applicationId = applicationId;
    this.objectId = objectId;

    forceCacheReload();
  }

  public S3CachedObject(AmazonS3 s3, String bucket, String prefix, String applicationId, String objectId, TypeReference<T> typeReference) {
    super(typeReference);
    Objects.requireNonNull(s3);
    Objects.requireNonNull(bucket);
    Objects.requireNonNull(applicationId);
    Objects.requireNonNull(objectId);

    if (applicationId.startsWith("/") || objectId.startsWith("/") || objectId.endsWith("/")) {
      throw new IllegalArgumentException("Neither applicationId nor objectId may start with / and objectId may not end with it");
    }

    this.s3 = s3;
    this.bucket = bucket;
    this.prefix = StringUtils.trimToEmpty(prefix);
    this.applicationId = applicationId;
    this.objectId = objectId;

    forceCacheReload();
  }

  private String fullPath() {
    StringBuilder sb = new StringBuilder();
    sb.append(prefix);
    if (!prefix.endsWith("/")) {
      sb.append("/");
    }
    sb.append(applicationId);
    if (!applicationId.endsWith("/")) {
      sb.append("/");
    }
    sb.append(objectId);
    return sb.toString();
  }

  @Override
  protected T loadObjectFromStore() {
    T rval = null;
    LOG.debug("Force-reading cache from s3");

    try {
      String path = fullPath();
      S3Object sob = s3.getObject(bucket, path);

      rval = deserialize(IOUtils.toString(sob.getObjectContent()));
    } catch (AmazonS3Exception ase) {
      if (ase.getErrorCode().equals("404")) {
        LOG.debug("No such file, returning null");
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Error reading content", ioe);
    }
    return rval;
  }

  @Override
  protected final void saveCacheToStore(String jsonValue) {
    String fullPath = fullPath();
    if (jsonValue == null) {
      LOG.warn("Removing value from s3");
      s3.deleteObject(bucket, fullPath);
      LOG.info("Deleted object for {}/{} ({})", applicationId, objectId, fullPath);
    } else {
      byte[] data = jsonValue.getBytes();
      ObjectMetadata omd = new ObjectMetadata();
      omd.setContentType("application/json");
      omd.setContentLength(data.length);
      PutObjectRequest por = new PutObjectRequest(bucket, fullPath, new ByteArrayInputStream(data), omd);
      s3.putObject(por);
      LOG.info("Updated object for {}/{} ({})", applicationId, objectId, fullPath);
    }
  }

  public String getApplicationId() {
    return applicationId;
  }

  public String getObjectId() {
    return objectId;
  }

  public AmazonS3 getS3() {
    return s3;
  }

  public String getBucket() {
    return bucket;
  }

  public String getPrefix() {
    return prefix;
  }
}
