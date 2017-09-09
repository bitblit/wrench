package com.erigir.wrench.aws.wrench;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.erigir.wrench.sos.ObjectStorageImplementation;
import com.erigir.wrench.sos.StoredObjectMetadata;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: chrweiss
 * Date: 12/28/13
 * Time: 10:41 PM
 */
public class S3ObjectStorageImplementation implements ObjectStorageImplementation {
  private static final Logger LOG = LoggerFactory.getLogger(S3ObjectStorageImplementation.class);

  private AmazonS3 s3;
  private String bucketName;
  private String prefix;

  @Override
  public String toFullKey(Class clazz, String key) {
    return StringUtils.trimToEmpty(prefix) + clazz.getName() + "/" + key;
  }

  @Override
  public void storeBytes(String fullPath, InputStream zipped) {
    if (zipped != null && fullPath != null) {
      try {

        Map<String, String> userMeta = new TreeMap<String, String>();
        //userMeta.put("className", clazz.getName());

        ObjectMetadata omd = new ObjectMetadata();
        //omd.setContentLength(zipped.length);
        omd.setContentType("application/x-zip-json");
        omd.setContentMD5(new String(Base64.encodeBase64(DigestUtils.md5(zipped))));
        omd.setUserMetadata(userMeta);

        LOG.info("Storing object to :{}", new Object[] {fullPath});

        s3.putObject(bucketName, fullPath, zipped, omd);
      } catch (Exception e) {
        throw new IllegalArgumentException("Error storing object", e);
      }
    }
  }

  @Override
  public <T> Map<String, StoredObjectMetadata> listObjects(Class<T> type) {
    LOG.info("Fetching all data for class {}", type);
    Long start = System.currentTimeMillis();

    String fullPrefix = StringUtils.trimToEmpty(prefix) + type.getName();
    int prefixLength = fullPrefix.length() + 1;

    ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().
        withBucketName(bucketName).
        withPrefix(fullPrefix));

    Map<String, StoredObjectMetadata> rval = new TreeMap<String, StoredObjectMetadata>();
    for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
      String subKey = os.getKey().substring(prefixLength);
      if (subKey.length() > 0) {
        StoredObjectMetadata next = new StoredObjectMetadata();
        next.setKey(subKey);
        next.setModified(os.getLastModified());
        next.setType(type);
        rval.put(subKey, next);
      } else {
        LOG.debug("Skipping the directory itself");
      }
    }

    LOG.info("Fetched {} summaries in {} ms", rval.size(), System.currentTimeMillis() - start);

    return rval;
  }

  @Override
  public void deleteObject(String full) {
    try {
      LOG.info("Deleting {}", full);
      s3.deleteObject(bucketName, full);

    } catch (Exception e) {
      throw new IllegalStateException("Error deleting object " + full, e);
    }
  }

  @Override
  public InputStream readBytes(String fullPath) {
    InputStream rval = null;
    if (fullPath != null) {
      try {
        LOG.info("Attempting to load : {}", fullPath);
        S3Object o = s3.getObject(bucketName, fullPath);
        if (o != null) {
          rval = o.getObjectContent();
        }
      } catch (Exception e) {
        LOG.warn("Error loading object", e);
        rval = null;
      }
    }
    return rval;
  }

  public void setS3(AmazonS3 s3) {
    this.s3 = s3;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }
}
