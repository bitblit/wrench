package com.erigir.wrench.web.filedownload;

import com.erigir.wrench.QuietUtils;
import lombok.Builder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A class that can handle downloading a file in multiple chunks over time
 * <p>
 * NOTE!  This downloads INTO MEMORY - this is meant to pull smallish chunks and save them
 * This should NOT be used with a big value of chunkSizeInBytes since it will certainly
 * cause memory issues
 * <p>
 * Created by cweiss on 6/8/17.
 */
@Builder
public class RestartingFileDownloader {
  private static final Logger LOG = LoggerFactory.getLogger(RestartingFileDownloader.class);
  private static final String SALT = "banana"; // NOTE: This isnt supposed to be hard, its just supposed to be random

  private Long chunkSizeInBytes;
  private int connectTimeout;
  private int readTimeout;
  private File tempFolder;

  private int backoffSeconds;
  private int maxBackoffSeconds;
  private int maxFailuresBeforeAbort; // This is not on a per file basis, but the whole-file


    /*
     * Just here for testing
     * @param args
     *
    public static void main(String[] args) {
        LastDitchExceptionHandler.addHandler();

        try
        {
            RestartingFileDownloader inst = RestartingFileDownloader.builder()
                    .chunkSizeInBytes(250000L)
                    .connectTimeout(5000)
                    .readTimeout(15000)
                    .backoffSeconds(1)
                    .maxFailuresBeforeAbort(10)
                    .maxBackoffSeconds(15)
                    .tempFolder(new File("/Users/cweiss/adomni/temp"))
                    .build();

            File temp = new File(inst.tempFolder,"test.mp4");


            inst.downloadFile("https://adomni-php.s3.amazonaws.com/aasdf/591b95ba4a202jy5tEPqMbcbn11MDEjKeHEnj6dtrzTN4cWbmd_original.mp4",
                    temp);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/

  /**
   * Generates the md5 of the given file as a hex string
   *
   * @param file File to md5
   * @return String containing the hex md5 of the file, null if file does not exist or is not readable
   */
  public static String md5File(File file) {
    Objects.requireNonNull(file);
    try (FileInputStream fis = new FileInputStream(file)) {
      return DigestUtils.md5Hex(fis);
    } catch (IOException ioe) {
      LOG.warn("Error trying to md5 this file : {}", file, ioe);
      return null;
    }
  }

  /**
   * Given a list of files, joins them together efficiently
   *
   * @param destination File to write the joined files as
   * @param sources     List of files to join
   * @return true if successful, false otherwise
   */
  public static boolean joinFiles(File destination, File[] sources) {
    boolean success = false;
    OutputStream output = null;
    try {
      output = createAppendableStream(destination);
      for (File source : sources) {
        appendFile(output, source);
      }
      success = true;
    } catch (IOException ioe) {
      LOG.warn("Failed to join files", ioe);
    } finally {
      IOUtils.closeQuietly(output);
    }
    return success;
  }

  /**
   * Creates an appendable, buffered output stream
   *
   * @param destination File to create the stream pointing to
   * @return BufferedOutputStream to write
   * @throws FileNotFoundException Not sure why java would throw this here
   */
  private static BufferedOutputStream createAppendableStream(File destination)
      throws FileNotFoundException {
    return new BufferedOutputStream(new FileOutputStream(destination, true));
  }

  /**
   * Add the contents of a file to an outputstream
   *
   * @param output Outputstream to write to
   * @param source File to read from
   * @throws IOException on error
   */
  private static void appendFile(OutputStream output, File source)
      throws IOException {
    InputStream input = null;
    try {
      input = new BufferedInputStream(new FileInputStream(source));
      IOUtils.copy(input, output);
    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  /**
   * Makes multiple attempts to download URL and save it to target.
   * <p>
   * Server MUST support partial transfers for this to work - fortunately S3 does.
   * <p>
   * If target exists it will be overwritten
   * Target is not created until the end so if it exists it is safe to use
   *
   * @param url         String containing the url to download
   * @param target      File to save the completed download
   * @param forceUpdate boolean if true, ALWAYS downloads, even if the local file is up to date
   * @throws ZeroLengthContentSpecifiedException        The content exists but has 0 length
   * @throws TooManyFailuresAttemptingDownloadException If N tries of the whole download were made and failed
   * @throws MissingContentSpecifiedException           The url specified 404s
   */
  public void downloadFile(String url, File target, boolean forceUpdate)
      throws ZeroLengthContentSpecifiedException, TooManyFailuresAttemptingDownloadException, MissingContentSpecifiedException {
    long start = System.currentTimeMillis();
    Objects.requireNonNull(url);
    Objects.requireNonNull(target);
    LOG.info("Attempting download of {} to {}", url, target);

    String prefix = DigestUtils.md5Hex((url + SALT).getBytes());
    File chunkDir = new File(tempFolder, prefix);
    boolean success = false;
    int failCount = 0;
    while (!success) {
      if (failCount > maxFailuresBeforeAbort) {
        // Just in case there is something fundamentally wrong in the file somehow
        throw new TooManyFailuresAttemptingDownloadException(url);
      }

      ContentDetails cd = fetchContentDetails(url);
      if (cd.contentLength == 0) {
        throw new ZeroLengthContentSpecifiedException(url);
      }

      if (forceUpdate || !target.exists() || cd.lastModified == null || cd.lastModified.isAfter(Instant.ofEpochMilli(target.lastModified()))) {
        LOG.debug("Creating chunk directory");
        chunkDir.mkdirs();


        long firstByte = 0L;
        List<File> allFiles = new LinkedList<>();
        while (firstByte < cd.contentLength) {
          long nextFirstByte = Math.min(firstByte + chunkSizeInBytes, cd.contentLength);
          long expectedLength = nextFirstByte - firstByte;

          File chunkTarget = new File(chunkDir, firstByte + ".chunk");

          // If machine dies during write to disk we can get a bad chunk, check length
          if (chunkTarget.exists() && chunkTarget.length() == expectedLength) {
            LOG.debug("Found {} already downloaded - continuing", chunkTarget);
            firstByte = nextFirstByte;
            allFiles.add(chunkTarget);
          } else {
            int pct = (int) (firstByte * 100 / cd.contentLength);
            LOG.debug("Downloading from {} to {} (of {} - {}%)", firstByte, nextFirstByte, cd.contentLength, pct);

            String rangeHeader = "bytes=" + firstByte + "-" + (nextFirstByte - 1);

            try {
              Request.Get(url)
                  .connectTimeout(connectTimeout)
                  .socketTimeout(readTimeout)
                  .addHeader("Range", rangeHeader)
                  .execute()
                  .saveContent(chunkTarget);
              firstByte = nextFirstByte;
              allFiles.add(chunkTarget);
              resetBackoff();
            } catch (Exception e) {
              LOG.info("Error attempting to download - will retry", e);
              backoff();
            }
          }
        }

        // Now join the files
        LOG.debug("Joining {} files", allFiles.size());
        File fullTemp = new File(tempFolder, prefix + ".fulltemp");
        fullTemp.delete(); // In case its already there
        int joinFailures = 0;
        while (!joinFiles(fullTemp, allFiles.toArray(new File[0]))) {
          LOG.warn("Failed to join files - retrying");
          fullTemp.delete();
          joinFailures++;
          if (joinFailures > maxFailuresBeforeAbort) {
            throw new TooManyFailuresAttemptingDownloadException(url);
          }
        }

        if (fullTemp.length() == cd.contentLength) {
          LOG.debug("Files joined and correct size, src size={} dst size={}", cd.contentLength, fullTemp.length());
          if (cd.etag == null) {
            LOG.warn("Url {} has no MD5 to check so passing it - renaming {} to {}", url, fullTemp, target);
            target.delete();
            saferFileRename(fullTemp, target);
            success = true;
          } else {
            LOG.debug("Attempting MD5 check");
            String md5 = md5File(fullTemp);
            LOG.debug("Valid hash - file md5:{} Src:{}", md5, cd.etag);
            if (md5.equalsIgnoreCase(cd.etag)) {
              LOG.info("Successfully download {} to {} - renaming to {}", url, fullTemp, target);
              target.delete();
              saferFileRename(fullTemp, target);
              success = true;
            } else {
              LOG.warn("MD5 mismatch found {} expected {} - reloading content", md5, cd.etag);
              failCount++;
            }
          }
        } else {
          LOG.warn("Size mismatch - reloading content expected={} found={}", cd.contentLength, fullTemp.length());
          failCount++;
        }

        // Whether a success or failure, delete the old chunk directory if we reached here
        LOG.debug("Deleting old chunk directory");
        FileUtils.deleteQuietly(chunkDir);
      } else {
        LOG.info("Current file {} is up-to-date (remote is modified {}) and forceUpdate not set, leaving alone", target, cd.lastModified);
        success = true;
      }

    }

    long duration = System.currentTimeMillis()-start;
    LOG.info("Completed download of {} to {} in {}", url, target, DurationFormatUtils.formatDurationHMS(duration));
  }

  /**
   * Waits a exponentially increasing amount of time (used when a http request fails)
   */
  private void backoff() {
    LOG.debug("Performing backoff, {} seconds", backoffSeconds);
    QuietUtils.quietSleep(backoffSeconds * 1000);
    backoffSeconds = Math.min(backoffSeconds * 2, maxBackoffSeconds); // exponential backoff
  }

  /**
   * Resets the backoff wait (used when an http request succeeds)
   */
  private void resetBackoff() {
    backoffSeconds = 1;
  }

  /**
   * Does a HEAD request on the url to fetch the etag (md5), content length, and last modified values
   * Also verifies existence
   *
   * @param url String containing the url to test
   * @return ContentDetails of that url
   */
  public ContentDetails fetchContentDetails(String url) {
    ContentDetails rval = null;

    while (rval == null) {
      try {
        // If this gives a 404 or any valid response this will return - otherwise it will spin forever
        // trying to fetch to content type which should only happen when no internet connection is available

        LOG.debug("Attempting to read content details for {}", url);

        rval = Request.Head(url).connectTimeout(connectTimeout)
            .socketTimeout(readTimeout)
            .execute()
            .handleResponse(new ResponseHandler<ContentDetails>() {
              @Override
              public ContentDetails handleResponse(HttpResponse tx) throws ClientProtocolException, IOException {
                if (tx.getStatusLine().getStatusCode() == 404) {
                  LOG.warn("Requested 404 url {} as content", url);
                  throw new MissingContentSpecifiedException(url);
                } else {
                  ContentDetails rval = new ContentDetails();
                  rval.etag = (tx.getFirstHeader("ETag") == null) ? null : StringUtils.trimToNull(tx.getFirstHeader("ETag").getValue());
                  rval.contentLength = (tx.getFirstHeader("Content-Length") == null) ? null : Long.parseLong(tx.getFirstHeader("Content-Length").getValue());
                  rval.lastModified = (tx.getFirstHeader("Last-Modified") == null) ? null : Instant.ofEpochMilli(DateUtils.parseDate(tx.getFirstHeader("Last-Modified").getValue()).getTime());

                  if (rval.etag != null) {
                    // AMZN wraps in double quotes
                    if (rval.etag.startsWith("\"") && rval.etag.endsWith("\"") && rval.etag.length() > 1) {
                      rval.etag = rval.etag.substring(1, rval.etag.length() - 1);
                    }
                  }

                  return rval;
                }
              }
            });
        resetBackoff();
      } catch (MissingContentSpecifiedException mce) {
        // Rethrow
        throw mce;
      } catch (Exception e) {
        LOG.warn("Could not read content details", e);
        backoff();
        rval = null;
      }
    }
    return rval;
  }

  /**
   * Mainly here because windows does not handle this well if the file is locked at all
   *
   * @param src File to copy from
   * @param dst File to copy to
   */
  private void saferFileRename(File src, File dst) {
    int moveRetryCount = 4;
    long waitTime = 1000;
    boolean complete = false;

    while (!complete && moveRetryCount > 0) {
      try {
        moveRetryCount--;
        Files.move(src.toPath(), dst.toPath(), StandardCopyOption.ATOMIC_MOVE);
        // If we reached here, great!
        complete = (dst.exists() && dst.length() > 0);
      } catch (IOException ioe) {
        LOG.warn("Error attempting to rename file : {}", ioe.getMessage());
        complete = false;
      }
      if (!complete) {
        LOG.info("Rename failed - sleeping {} ms and retrying {} more times", waitTime, moveRetryCount);
        QuietUtils.quietSleep(waitTime);
        waitTime *= 2; // Exponential backoff
      }
    }

    if (!complete) {
      LOG.error("Giving up on file rename of {} to {}", src, dst);
      throw new RuntimeException("Unable to rename file " + src + " to " + dst + " correctly");
    }
  }

  /**
   * Simple wrapper for some information about a url
   */
  public static class ContentDetails {
    String etag;
    Long contentLength;
    Instant lastModified;
  }

}
