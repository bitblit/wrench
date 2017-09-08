package com.erigir.wrench.ape.exception;

import com.erigir.wrench.ape.http.ApeException;
import com.fasterxml.jackson.core.JsonLocation;

/**
 * Created by chrweiss on 6/28/14.
 */
@ApeException(
    httpStatusCode = 400,
    detailCode = 100,
    message = "There was a problem with the data submitted",
    developerMessage = "The JSON submitted does not match the schema for this endpoint",
    detailObjectPropertyName = "location"
)
public class BadJsonException extends RuntimeException {
  private JsonLocation location;

  public BadJsonException(JsonLocation location) {
    this.location = location;
  }

  public JsonLocation getLocation() {
    return location;
  }
}
