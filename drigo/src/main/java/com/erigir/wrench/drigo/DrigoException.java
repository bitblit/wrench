package com.erigir.wrench.drigo;

/**
 * Superclass of any exception thrown by Drigo - its a batch processor, so
 * this are all Runtime by default
 * <p>
 * Created by cweiss on 8/5/15.
 */
public class DrigoException extends RuntimeException {
  public DrigoException() {
  }

  public DrigoException(String message) {
    super(message);
  }

  public DrigoException(String message, Throwable cause) {
    super(message, cause);
  }

  public DrigoException(Throwable cause) {
    super(cause);
  }

  public DrigoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
