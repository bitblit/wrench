package com.erigir.wrench.ape.exception;

import com.erigir.wrench.ape.http.ApeException;

/**
 * Created by chrweiss on 6/28/14.
 */
@ApeException(
    httpStatusCode = 403,
    detailCode = 101,
    message = "The credentials provided are insufficient for this request",
    developerMessage = "You did not provide enough credentials for your request",
    detailObjectPropertyName = "required"
)
public class InsufficientCredentialsException extends RuntimeException {
  private String required;

  public InsufficientCredentialsException(String required) {
    this.required = required;
  }

  public String getRequired() {
    return required;
  }
}
