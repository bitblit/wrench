package com.erigir.wrench.aws.cloudwatch.logs.logupload;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.model.CreateLogGroupRequest;
import com.amazonaws.services.logs.model.CreateLogStreamRequest;
import com.amazonaws.services.logs.model.DeleteLogStreamRequest;
import com.amazonaws.services.logs.model.InputLogEvent;
import com.amazonaws.services.logs.model.PutLogEventsRequest;
import com.amazonaws.services.logs.model.PutLogEventsResult;
import com.amazonaws.services.logs.model.RejectedLogEventsInfo;
import com.amazonaws.services.logs.model.ResourceAlreadyExistsException;
import com.amazonaws.services.logs.model.ResourceNotFoundException;
import com.erigir.wrench.Canonicalizer;
import lombok.Builder;
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
    for (File f:toUpload)
    {
      try
      {
        uploadFile(f);
      }
      catch(Exception e)
      {
        LOG.error("Error attempting to upload file {} - leaving in place",f,e);
      }
    }
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

  private void uploadFile(File f)
      throws Exception
  {
    LOG.info("Uploading file {}",f);
    long startPos = 0;
    if (f.length()>maxFileSize)
    {
      LOG.info("Warning! File is {} bytes and max size is {} - file will be truncated to the last portion",f.length(), maxFileSize);
      startPos = f.length()-maxFileSize;
    }
    LOG.debug("Finding log group");

    String logStreamName = createLogStreamName(f);
    if (clearAndVerifyLogStream(logStreamName))
    {
      LOG.info("Log stream verified - beginning upload");

      try (BufferedReader br = new BufferedReader(new InputStreamReader(openAtLocation(f, startPos)))){
        String sequenceToken = null;
        List<InputLogEvent> events = buildNextList(br);
        while (events.size()>0)
        {
          // We sort because once in a great while SLF4J gets them out of order, and cloudwatch does
          // not like that at all
          Collections.sort(events, new Comparator<InputLogEvent>() {
            @Override
            public int compare(InputLogEvent o1, InputLogEvent o2) {
              return (int)(o1.getTimestamp()-o2.getTimestamp());
            }
          });

          eventsInOrder(events);
            LOG.debug("Writing batch of {} events", events.size());
            PutLogEventsRequest pler = new PutLogEventsRequest().withLogEvents(events)
                .withLogGroupName(logGroup).withLogStreamName(logStreamName)
                .withSequenceToken(sequenceToken);
            PutLogEventsResult result = logger.putLogEvents(pler);
            RejectedLogEventsInfo rei = result.getRejectedLogEventsInfo();
            if (rei!=null)
            {
              if (rei.getTooNewLogEventStartIndex()!=null || rei.getTooOldLogEventEndIndex()!=null)
              {
                LOG.info("Rejected {} events as too old and {} as too new",rei.getTooOldLogEventEndIndex(),rei.getTooNewLogEventStartIndex());
              }
            }
            sequenceToken = result.getNextSequenceToken();
            // Any failure will cause the whole file to be retried later
            events = buildNextList(br);
        }

        // If we reached here, the file was uploaded
        LOG.info("Successfully uploaded file {}",f);
        br.close();

        if (deleteOnSuccessfulUpload)
        {
          LOG.info("Deleting file {}",f);
          if (!f.delete())
          {
            LOG.info("Failed to delete {} - will try again on exit");
            f.deleteOnExit();
          }
        }
      }
      catch (Exception e)
      {
        LOG.info("Error reading file",e);
      }
    }

  }

  private boolean eventsInOrder(List<InputLogEvent> events)
  {
    boolean rval=true;

    for (int i=1;i<events.size();i++)
    {
      if (events.get(i).getTimestamp()<events.get(i-1).getTimestamp())
      {
        rval = false;
        LOG.info("EOOO : {}\n\n{}\n\n",events.get(i),events.get(i-1));
      }
    }
    return rval;
  }


  private List<InputLogEvent> buildNextList(BufferedReader br)
      throws IOException
  {
    List<InputLogEvent> rval = new LinkedList<>();
    long totalSize = 0;

    InputLogEvent next = fetchNextEvent(br);
    while (next!=null && totalSize<(1024*256) && rval.size()<5000) //approx 256K packages
    {
      rval.add(next);
      totalSize+=next.getMessage().length();
      next = fetchNextEvent(br);
    }

    if(next!=null)
    {
      rval.add(next); //grab the last one
    }
    return rval;

  }


  private InputLogEvent fetchNextEvent(BufferedReader br)
      throws IOException
  {
    InputLogEvent rval = null;

    String line = null;
    if (lookAhead==null)
    {
      line = br.readLine();
    }
    else
    {
      line = lookAhead;
      lookAhead = null;
    }

    if (line!=null)
    {
      Date ts = extractTimestamp(line);
      if (ts!=null)
      {
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        lookAhead = br.readLine();
        while (lookAhead!=null && extractTimestamp(lookAhead)==null)
        {
          sb.append("\n").append(lookAhead);
          lookAhead = br.readLine();
        }
        String msg = sb.toString();
        if (msg.length()>256000)
        {
          // Really its 262144 but giving myself some slack
          LOG.warn("Truncating entry - line was {} bytes of a max of {}",msg.length(),  256000);
          msg = msg.substring(0,256000);
        }
        rval = new InputLogEvent().withTimestamp(ts.getTime()).withMessage(msg);
      }
    }
    return rval;
  }

  private Date extractTimestamp(String value)
  {
    Date rval = null;
    if (StringUtils.trimToNull(value)!=null)
    {
      if (value.length()>dateFormat.length())
      {
        try
        {
          rval = new SimpleDateFormat(dateFormat).parse(value.substring(0,dateFormat.length()));
        }
        catch (Exception e)
        {
          LOG.trace("Failed to parse {} as date",value);
        }
      }
    }
    return rval;
  }

  private FileInputStream openAtLocation(File f, long startPos)
      throws IOException
  {
    FileInputStream fis = new FileInputStream(f);
    // Seek to the first CR after start position
    long cnt = 0;
    int chr = -1;
    while (cnt<startPos)
    {
      chr = fis.read();
      cnt++;
    }
    while (chr!=10 && chr!=-1 && chr!=13)
    {
      chr = fis.read();
    }

    return fis;
  }

  private String createLogStreamName(File f)
  {
    return StringUtils.trimToEmpty(this.prefix)+ Canonicalizer.canonicalize(f.getName());
  }

  private void verifyLogGroup()
  {
    if (!logGroupVerified) {
      try {
        CreateLogGroupRequest r = new CreateLogGroupRequest().withLogGroupName(logGroup);
        logger.createLogGroup(r);
        LOG.info("Created log group {}", this.logGroup);
        logGroupVerified = true;
      } catch (ResourceAlreadyExistsException rae) {
        LOG.info("Log group is already present");
        logGroupVerified = true;
      } catch (Exception e) {
        LOG.info("Unexpected exception trying to verify log group", e);
      }
    }
    else
    {
      LOG.trace("Skipping - log group already verified");
    }
  }

  private boolean clearAndVerifyLogStream(String logStream)
  {
    boolean verified = true;

    verifyLogGroup();
    LOG.info("Verifying log stream");

    LOG.info("Deleting log stream if present");
    try
    {
      DeleteLogStreamRequest dlsr = new DeleteLogStreamRequest().withLogGroupName(logGroup).withLogStreamName(logStream);
      logger.deleteLogStream(dlsr);
      LOG.info("Log stream deleted");
    }
    catch (ResourceNotFoundException rnfe)
    {
      LOG.debug("Skipping - resource not found");
    }
    catch (Exception e)
    {
      LOG.warn("Failed to delete log stream",e);
      verified = false;
    }
    if (verified)
    {
      try {
        LOG.info("Creating new log stream {}",logStream);
        CreateLogStreamRequest r = new CreateLogStreamRequest().withLogGroupName(logGroup).withLogStreamName(logStream);
        logger.createLogStream(r);
      }
      catch (Exception e)
      {
        LOG.info("Unexpected exception trying to verify log stream",e);
        verified = false;
      }
    }
    return verified;
  }


  public static void main(String[] args) {
    try
    {

      DefaultAWSCredentialsProviderChain awsCred = new DefaultAWSCredentialsProviderChain();
      AWSLogs logs = AWSLogsClient.builder().withCredentials(awsCred).build();

      LogFileUploader l = LogFileUploader.builder()
          .logger(logs)
          .deleteOnSuccessfulUpload(false)
          .logGroup("testLogGroup")
          .logFolder(new File("/tmp/testlog"))
          .prefix("testPrefix-")
          .build();

      l.performUpload();

    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

}
