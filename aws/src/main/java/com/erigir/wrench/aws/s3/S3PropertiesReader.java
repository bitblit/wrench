package com.erigir.wrench.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by chrweiss on 6/1/15.
 */
public class S3PropertiesReader {
    private static final Logger LOG = LoggerFactory.getLogger(S3PropertiesReader.class);
    private AmazonS3 s3;

    public Properties loadFromS3(String bucket, String path) {
        Objects.requireNonNull(s3, "S3 must be set before running");
        Objects.requireNonNull(bucket, "You must provide a bucket");
        Objects.requireNonNull(path, "You must provide a path");

        Properties rval = null;
        try {
            LOG.debug("Attempting to load properties object from s3 : {}/{}", bucket, path);
            S3Object sob = s3.getObject(bucket, path);
            rval = new Properties();
            rval.load(sob.getObjectContent());
        } catch (AmazonS3Exception s3e) {
            LOG.debug("No such s3 object found {}/{} returning null", bucket, path);
            rval = null;
        } catch (IOException ioe) {
            LOG.warn("IOException trying to read bucket, passing up failure");
            throw new RuntimeException(ioe);
        }
        return rval;
    }

    public void setS3(AmazonS3 s3) {
        this.s3 = s3;
    }
}
