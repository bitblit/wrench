package com.erigir.wrench.aws.cloudfront.logparser;

import com.erigir.wrench.aws.cloudfront.logparser.handler.ChainHandler;
import com.erigir.wrench.aws.cloudfront.logparser.handler.FieldRegexFilter;
import com.erigir.wrench.aws.cloudfront.logparser.handler.SimplePrintWriterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;

/**
 * A simple tool for doing grep-style stuff through the access logs stored by Cloudfront.
 *
 * It can process either a local file/directory or a s3 bucket/prefix.  If you are doing a
 * single run it can make sense to hit s3 directly, but for extensive querying it makes more
 * sense to download and run.  See:
 * &gt; sudo easy_install awscli
 * &gt; aws s3 sync s3://mybucket .
 *
 * Created by chrweiss on 3/16/15.
 */
public class CloudFrontAccessLogParser {
    private static final Logger LOG = LoggerFactory.getLogger(CloudFrontAccessLogParser.class);

    public static void main(String[] args) {
        try
        {
            if (args.length<1 || args.length>2)
            {
                System.out.println("Usage: CloudFrontAccessLogParser {file/dir} {optional regular expression}");
            }
            else
            {
                ChainHandler handler = new ChainHandler();
                if (args.length>1)
                {
                    handler.addHandler(new FieldRegexFilter(CloudFrontAccessLogField.RAW,args[1]));
                }
                handler.addHandler(new SimplePrintWriterHandler(new PrintWriter(System.out)));

                new CloudFrontAccessLogParser().processLogs(new File(args[0]), handler);
            }
        }
        catch (Exception e)
        {
            LOG.error("Failed to parse logs",e);
        }
    }

    public void processLogs(String s3Bucket, String s3Prefix, CloudFrontAccessLogHandler handler)
    {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void processLogs(File file, CloudFrontAccessLogHandler handler)
    {
        if (file!=null && file.exists())
        {
            if (file.isDirectory())
            {
                for (String s:file.list())
                {
                    processLogs(new File(file, s),handler);
                }
            }
            else // its a file
            {
                LOG.debug("Processing file {}",file);
                try {
                    InputStream src = new FileInputStream(file);
                    if (file.getAbsolutePath().toLowerCase().endsWith(".gz")) {
                        LOG.debug("Processing as gzip file");
                        src = new GZIPInputStream(src);
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader(src));
                    readLogStream(br, handler);
                    br.close();
                }
                catch (IOException ioe)
                {
                    LOG.warn("Error processing file {}, continuing", file, ioe);
                }
            }
        }
        else
        {
            LOG.debug("Not processing - null or non-existant file passed");
        }
    }

    private void readLogStream(BufferedReader br, CloudFrontAccessLogHandler handler)
            throws IOException
    {
            String next = br.readLine();
            while (next!=null)
            {
                if (!next.startsWith("#")) { // strip comment lines
                    CloudFrontAccessLogEntry entry = new CloudFrontAccessLogEntry(next);
                    handler.handleCloudFrontAccessLogEntry(entry);
                }
                next = br.readLine();
            }
    }

}
