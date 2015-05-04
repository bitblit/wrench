package com.erigir.wrench.aws.cloudfront.logparser;

/**
 * Created by chrweiss on 3/16/15.
 */
public interface CloudFrontAccessLogHandler {
    boolean handleCloudFrontAccessLogEntry(CloudFrontAccessLogEntry entry);
}
