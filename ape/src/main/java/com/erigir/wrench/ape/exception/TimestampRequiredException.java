package com.erigir.wrench.ape.exception;

import com.erigir.wrench.ape.http.ApeException;

/**
 * Created by chrweiss on 6/28/14.
 */
@ApeException(
    httpStatusCode = 400,
    detailCode = 105,
    message = "The X-TIMESTAMP header must be supplied in such requests",
    developerMessage = "You must provide the X-TIMESTAMP header in signed requests (it is part of the signed package)"
)
public class TimestampRequiredException extends RuntimeException {

}
