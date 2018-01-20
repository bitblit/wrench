package com.erigir.wrench.web.filedownload;

/**
 * Created by cweiss on 6/8/17.
 */
public class TooManyFailuresAttemptingDownloadException extends RuntimeException {
  private String url;

  public TooManyFailuresAttemptingDownloadException(String url) {
    this.url = url;
  }

  public TooManyFailuresAttemptingDownloadException(String message, String url) {
    super(message);
    this.url = url;
  }

  public TooManyFailuresAttemptingDownloadException(String message, Throwable cause, String url) {
    super(message, cause);
    this.url = url;
  }

  public TooManyFailuresAttemptingDownloadException(Throwable cause, String url) {
    super(cause);
    this.url = url;
  }

  public TooManyFailuresAttemptingDownloadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String url) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}
