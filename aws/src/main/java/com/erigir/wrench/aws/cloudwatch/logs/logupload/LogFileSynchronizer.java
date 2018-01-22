package com.erigir.wrench.aws.cloudwatch.logs.logupload;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.model.CreateLogGroupRequest;
import com.amazonaws.services.logs.model.CreateLogStreamRequest;
import com.amazonaws.services.logs.model.GetLogEventsRequest;
import com.amazonaws.services.logs.model.GetLogEventsResult;
import com.amazonaws.services.logs.model.InputLogEvent;
import com.amazonaws.services.logs.model.InvalidSequenceTokenException;
import com.amazonaws.services.logs.model.OutputLogEvent;
import com.amazonaws.services.logs.model.PutLogEventsRequest;
import com.amazonaws.services.logs.model.PutLogEventsResult;
import com.amazonaws.services.logs.model.RejectedLogEventsInfo;
import com.amazonaws.services.logs.model.ResourceAlreadyExistsException;
import com.erigir.wrench.Canonicalizer;
import com.erigir.wrench.QuietUtils;
import lombok.Builder;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Builder
public class LogFileSynchronizer {
  private static final Logger LOG = LoggerFactory.getLogger(LogFileUploader.class);

  private AWSLogs awsLogs;
  private String logGroup;
  private File logFolder;
  @Builder.Default
  private String prefix = "";
  @Builder.Default
  private String dateFormat = "yyyy-MM-dd hh:mm:ss,SSS";

  /*
  If realTimeSynchronize is false, then only files that are older (i.e., have not been modified)
  than this are uploaded.  In either case, if deleteOnSuccessfulUpload is set, it will only
  fire after the file is at least this old.
   */
  @Builder.Default
  private long quietDelayMS = 1000 * 60 * 60;
  /*
  Upload no more than 1 Mb per sync call by default (if the edge of the sync is further back than
  that, a truncation message will be logged
  */
  @Builder.Default
  private Long maxUploadPerCall = 20000L; //1024 * 1024 * 1L;
  /**
   * If true, will delete the local log file when the upload is complete - where complete is defined
   * as "everything is uploaded, and there have been no modifications to the file in at least
   * quietDelayMS MS"
   */
  @Builder.Default
  private boolean deleteFileWhenComplete = true;

  @Builder.Default
  private boolean logGroupVerified = false;
  @Builder.Default
  private String lookAhead = null;

  public static void main(String[] args) {
    try {

      DefaultAWSCredentialsProviderChain awsCred = new DefaultAWSCredentialsProviderChain();
      AWSLogs logs = AWSLogsClient.builder().withCredentials(awsCred).build();

      LogFileSynchronizer l = LogFileSynchronizer.builder()
          .awsLogs(logs)
          .deleteFileWhenComplete(false)
          .logGroup("testLogGroup")
          .logFolder(new File("/tmp/testlog"))
          .prefix("testPrefix-")
          .build();

      boolean aborted = false;
      while (!aborted) {
        l.performUpload();
        QuietUtils.quietSleep(10000);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void performUpload() {
    LOG.info("Running log file upload");
    performUpload(findFilesToUpload());
  }

  public void performUpload(List<File> toUpload) {
    if (toUpload.size() > 0) {
      LOG.info("{} Log files found - checking log group", toUpload.size());
      verifyLogGroup();
      LOG.info("Log group found - pumping files");
      for (File f : toUpload) {
        try {
          uploadFile(f);
        } catch (Exception e) {
          LOG.error("Error attempting to upload file {} - leaving in place", f, e);
        }
      }
    } else {
      LOG.info("Skipping - no files found to upload");
    }
  }

    private List<File> findFilesToUpload() {
    List<File> rval = new LinkedList<>();

    if (!logFolder.exists() || !logFolder.isDirectory())
    {
      throw new IllegalArgumentException(logFolder+" does not exist or is not a folder");
    }
    File[] files = logFolder.listFiles();
    for (File test : files) {
      if (test.exists() && test.isFile()) {
        rval.add(test);
      } else {
        LOG.debug("Skipping {} - not a file");
      }
    }

    return rval;
  }

  private List<OutputLogEvent> fetchLastEvents(String logStreamName, int limit)
  {
    GetLogEventsRequest gler = new GetLogEventsRequest().withLimit(limit).withLogGroupName(logGroup)
        .withLogStreamName(logStreamName);
    GetLogEventsResult result = awsLogs.getLogEvents(gler);
    return result.getEvents();
  }

  private void uploadFile(File f)
      throws Exception {

    String logStreamName = createLogStreamName(f);
    boolean createdLogStream = verifyLogStream(logStreamName);
    long startFilePos = 0;

    if (!createdLogStream) {
      LOG.info("Log file exists - finding seek pos");
      List<OutputLogEvent> events = fetchLastEvents(logStreamName,5);
      LOG.info("Got : {}",events);
      for (int i=events.size()-1;i>=0 && startFilePos==0;i--)
      {
        String msg = events.get(i).getMessage();
        if (msg.startsWith("-- LogFileSynchronizer "))
        {
          int idx1 = msg.indexOf(":");
          int idx2 = msg.indexOf(":",idx1+1);
          startFilePos = Long.parseLong(msg.substring(idx1+1,idx2));
        }
      }

    }

    long bytesToUpload = f.length() - startFilePos;

    if (bytesToUpload==0)
    {
      LOG.debug("Skipping upload - nothing to upload since last time");
    }
    else {

      if (bytesToUpload > maxUploadPerCall) {
        long skip = bytesToUpload - maxUploadPerCall;
        LOG.warn("Too much to upload - skipping forward {} bytes", skip);
        startFilePos = f.length() - maxUploadPerCall;
      }

      LOG.info("Beginning uploading file {}", f);
      CountingInputStream cis = new CountingInputStream(new FileInputStream(f));
      cis.skip(startFilePos);
      LOG.info("Pos : {}",cis.getByteCount());

      try ( BufferedReader br = new BufferedReader(new InputStreamReader(cis))) {
        String sequenceToken = null;
        seekToFirstEvent(br);
        List<InputLogEvent> events = buildNextList(br);
        InputLogEvent lastEvent = null;
        while (events.size() > 0) {
          // We sort because once in a great while SLF4J gets them out of order, and cloudwatch does
          // not like that at all
          Collections.sort(events, new Comparator<InputLogEvent>() {
            @Override
            public int compare(InputLogEvent o1, InputLogEvent o2) {
              return (int) (o1.getTimestamp() - o2.getTimestamp());
            }
          });

          eventsInOrder(events);
          LOG.debug("Writing batch of {} events", events.size());
          PutLogEventsRequest pler = new PutLogEventsRequest().withLogEvents(events)
              .withLogGroupName(logGroup).withLogStreamName(logStreamName)
              .withSequenceToken(sequenceToken);
          sequenceToken = retryingPutLogEvents(pler);
          lastEvent = events.get(events.size() - 1);
          // Any failure will cause the whole file to be retried later
          events = buildNextList(br);
        }

        // If we reached here, the file was uploaded
        LOG.info("Successfully uploaded file {}", f);
        Long lastEventTimestamp = (lastEvent != null) ? lastEvent.getTimestamp() : null;
        long lastByte = cis.getByteCount();
        br.close();

        long maxLastModified = System.currentTimeMillis() - quietDelayMS;
        if (f.lastModified() < maxLastModified) {
          if (deleteFileWhenComplete) {
            LOG.info("Deleting file {}", f);
            if (!f.delete()) {
              LOG.info("Failed to delete {} - will try again on exit");
              f.deleteOnExit();
            }
          }
        } else {
          LOG.info("File is too fresh - setting marker");
          writeLastLocationMarker(sequenceToken, logStreamName, lastEventTimestamp, lastByte);
        }


      } catch (Exception e) {
        LOG.info("Error reading file", e);
      }

    }

  }

  private String retryingPutLogEvents(PutLogEventsRequest pler)
      throws Exception {
    try {
      PutLogEventsResult result = awsLogs.putLogEvents(pler);
      RejectedLogEventsInfo rei = result.getRejectedLogEventsInfo();
      if (rei != null) {
        if (rei.getTooNewLogEventStartIndex() != null || rei.getTooOldLogEventEndIndex() != null) {
          LOG.info("Rejected {} events as too old and {} as too new", rei.getTooOldLogEventEndIndex(), rei.getTooNewLogEventStartIndex());
        }
      }
      return result.getNextSequenceToken();
    } catch (InvalidSequenceTokenException iste) {
      LOG.debug("Invalid sequence token found, setting and retrying");
      pler.setSequenceToken(iste.getExpectedSequenceToken());
      return retryingPutLogEvents(pler);
    }
  }

  private void writeLastLocationMarker(String sequenceToken, String logStreamName, Long lastEventTimestamp, long count)
      throws Exception
  {
    if (count>0 && lastEventTimestamp!=null)
    {
      PutLogEventsRequest pler = new PutLogEventsRequest().withLogEvents(Collections.singletonList(new InputLogEvent().withTimestamp(lastEventTimestamp).withMessage("-- LogFileSynchronizer :"+count+": bytes --")))
          .withLogGroupName(logGroup).withLogStreamName(logStreamName)
          .withSequenceToken(sequenceToken);
      PutLogEventsResult result = awsLogs.putLogEvents(pler);
      RejectedLogEventsInfo rei = result.getRejectedLogEventsInfo();
      if (rei != null) {
        if (rei.getTooNewLogEventStartIndex() != null || rei.getTooOldLogEventEndIndex() != null) {
          LOG.info("Rejected {} events as too old and {} as too new", rei.getTooOldLogEventEndIndex(), rei.getTooNewLogEventStartIndex());
        }
      }
    }
    else
    {
      LOG.warn("Weirdly, count was 0 or event timestamp was null - not writing end marker");
    }

  }

  private boolean eventsInOrder(List<InputLogEvent> events) {
    boolean rval = true;

    for (int i = 1; i < events.size(); i++) {
      if (events.get(i).getTimestamp() < events.get(i - 1).getTimestamp()) {
        rval = false;
        LOG.info("EOOO : {}\n\n{}\n\n", events.get(i), events.get(i - 1));
      }
    }
    return rval;
  }

  private List<InputLogEvent> buildNextList(BufferedReader br)
      throws IOException {
    List<InputLogEvent> rval = new LinkedList<>();
    long totalSize = 0;

    InputLogEvent next = fetchNextEvent(br);
    while (next != null && totalSize < (1024 * 256) && rval.size() < 5000) //approx 256K packages
    {
      rval.add(next);
      totalSize += next.getMessage().length();
      next = fetchNextEvent(br);
    }

    if (next != null) {
      rval.add(next); //grab the last one
    }
    return rval;

  }

  private void seekToFirstEvent(BufferedReader br)
      throws IOException
  {
    String line = br.readLine();
    while (extractTimestamp(line)==null && line!=null)
    {
      line = br.readLine();
    }

    // Put whatever we have is lookahead so it'll get consumed
    lookAhead = line;
  }

  private InputLogEvent fetchNextEvent(BufferedReader br)
      throws IOException {
    InputLogEvent rval = null;

    String line = null;
    if (lookAhead == null) {
      line = br.readLine();
    } else {
      line = lookAhead;
      lookAhead = null;
    }

    if (line != null) {
      Date ts = extractTimestamp(line);
      if (ts != null) {
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        lookAhead = br.readLine();
        while (lookAhead != null && extractTimestamp(lookAhead) == null) {
          sb.append("\n").append(lookAhead);
          lookAhead = br.readLine();
        }
        String msg = sb.toString();
        if (msg.length() > 256000) {
          // Really its 262144 but giving myself some slack
          LOG.warn("Truncating entry - line was {} bytes of a max of {}", msg.length(), 256000);
          msg = msg.substring(0, 256000);
        }
        rval = new InputLogEvent().withTimestamp(ts.getTime()).withMessage(msg);
      }
    }
    return rval;
  }

  private Date extractTimestamp(String value) {
    Date rval = null;
    if (StringUtils.trimToNull(value) != null) {
      if (value.length() > dateFormat.length()) {
        try {
          rval = new SimpleDateFormat(dateFormat).parse(value.substring(0, dateFormat.length()));
        } catch (Exception e) {
          LOG.trace("Failed to parse {} as date", value);
        }
      }
    }
    return rval;
  }

  private FileInputStream openAtLocation(File f, long startPos)
      throws IOException {
    FileInputStream fis = new FileInputStream(f);
    // Seek to the first CR after start position
    long cnt = 0;
    int chr = -1;
    while (cnt < startPos) {
      chr = fis.read();
      cnt++;
    }
    while (chr != 10 && chr != -1 && chr != 13) {
      chr = fis.read();
    }

    return fis;
  }

  private String createLogStreamName(File f) {
    return StringUtils.trimToEmpty(this.prefix) + Canonicalizer.canonicalize(f.getName());
  }

  private void verifyLogGroup() {
    if (!logGroupVerified) {
      try {
        CreateLogGroupRequest r = new CreateLogGroupRequest().withLogGroupName(logGroup);
        awsLogs.createLogGroup(r);
        LOG.info("Created log group {}", this.logGroup);
        logGroupVerified = true;
      } catch (ResourceAlreadyExistsException rae) {
        LOG.info("Log group is already present");
        logGroupVerified = true;
      } catch (Exception e) {
        LOG.info("Unexpected exception trying to verify log group", e);
      }
    } else {
      LOG.trace("Skipping - log group already verified");
    }
  }

  private boolean verifyLogStream(String logStream) {
    boolean created = false;

    verifyLogGroup();
    try {
      LOG.info("Attempting create new log stream {}", logStream);
      CreateLogStreamRequest r = new CreateLogStreamRequest().withLogGroupName(logGroup).withLogStreamName(logStream);
      awsLogs.createLogStream(r);
      created = true;
    } catch (ResourceAlreadyExistsException ree) {
      LOG.info("Log stream already exists");
    }

    return created;
  }

}
