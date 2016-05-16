package com.erigir.wrench.aws.cloudfront.logparser.handler;

import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogEntry;
import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogField;
import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogHandler;

import java.io.PrintWriter;

/**
 * Created by chrweiss on 3/16/15.
 */
public class SimplePrintWriterHandler implements CloudFrontAccessLogHandler {
    private PrintWriter writer;


    public SimplePrintWriterHandler() {
    }

    public SimplePrintWriterHandler(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public boolean handleCloudFrontAccessLogEntry(CloudFrontAccessLogEntry entry) {
        if (writer == null) {
            throw new IllegalStateException("You must set the writer before starting processing");
        }
        writer.println(entry.field(CloudFrontAccessLogField.RAW));
        return true;
    }
}
