package com.erigir.wrench.ape.exception;

import com.erigir.wrench.ape.http.ApeException;

/**
 * Created by chrweiss on 6/28/14.
 */
@ApeException(
    httpStatusCode = 403,
    detailCode = 100,
    message = "You must access this API over a secure network",
    developerMessage = "You attempted to access the API over HTTP.  Retry with a secure channel (HTTPS)"
)
public class HttpsRequiredException extends RuntimeException {

}
