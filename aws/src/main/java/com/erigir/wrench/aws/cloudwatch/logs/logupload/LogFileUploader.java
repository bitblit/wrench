package com.erigir.wrench.aws.cloudwatch.logs.logupload;

import com.amazonaws.services.logs.AWSLogs;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Yeah, I know I just created this class in the last release.  Deprecating it so that I do not have
 * to do a major version number for backwards compat - but I recommend you use LogFileSynchronizer
 * instead - it does everything this one does, but can do real time synchronization.  This class
 * is no longer maintained (CW - 01-22-2018)
 *
 * This class has been refactored so it just uses it under the covers anyway
 */
@Deprecated
@Builder
public class LogFileUploader {
  private static final Logger LOG = LoggerFactory.getLogger(LogFileUploader.class);

  private AWSLogs logger;
  private String logGroup;
  private File logFolder;
  @Builder.Default
  private String prefix="";
  @Builder.Default
  private String dateFormat="yyyy-MM-dd hh:mm:ss,SSS";
  // Only upload files that have been quiet for at least an hour
  @Builder.Default
  private long quietDelayMS = 1000*60*60;
  // No files larger than 256 Mb by default (only first 256 will be sent, plus a truncattion message)
  @Builder.Default
  private long maxFileSize = 1024*1024*256;
  @Builder.Default
  private boolean deleteOnSuccessfulUpload = true;

  @Builder.Default
  private boolean logGroupVerified = false;
  @Builder.Default
  private String lookAhead = null;


  public void performUpload()
  {
    long maxLastModified = System.currentTimeMillis()-quietDelayMS;
    LOG.info("Running log file upload - anything older than {}",new Date(maxLastModified));
    List<File> toUpload = findFilesToUpload(maxLastModified);

    LogFileSynchronizer lfs = LogFileSynchronizer.builder()
        .awsLogs(logger)
        .logGroup(logGroup)
        .logFolder(logFolder)
        .prefix(prefix)
        .dateFormat(dateFormat)
        .quietDelayMS(quietDelayMS)
        .maxUploadPerCall(maxFileSize)
        .deleteFileWhenComplete(deleteOnSuccessfulUpload)
        .build();

    lfs.performUpload(toUpload);
  }

  private List<File> findFilesToUpload(long maxLastModified)
  {
    List<File> rval = new LinkedList<>();

    for (String s:logFolder.list())
    {
      File test = new File(logFolder, s);
      if (test.exists() && test.isFile() && test.lastModified()<maxLastModified)
      {
        rval.add(test);
      }
      else
      {
        LOG.debug("Skipping {} - either not a file or too new", test);
      }
    }

    return rval;
  }
}
