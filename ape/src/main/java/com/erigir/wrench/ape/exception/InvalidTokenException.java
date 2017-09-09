package com.erigir.wrench.ape.exception;

import com.erigir.wrench.ape.http.ApeException;

/**
 * Created by cweiss on 7/18/15.
 */
@ApeException(
    httpStatusCode = 403,
    detailCode = 103,
    message = "Your token is invalid or expired",
    developerMessage = "You provided a token that is either invalid or expired - create a new one and try again"
)
public class InvalidTokenException extends Exception {

  public InvalidTokenException() {
  }

  public InvalidTokenException(String message) {
    super(message);
  }

  public InvalidTokenException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidTokenException(Throwable cause) {
    super(cause);
  }

  public InvalidTokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
