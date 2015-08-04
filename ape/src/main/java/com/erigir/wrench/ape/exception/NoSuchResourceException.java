package com.erigir.wrench.ape.exception;

import com.erigir.wrench.ape.http.ApeException;

/**
 * Created by chrweiss on 6/28/14.
 */
@ApeException(
        httpStatusCode = 404,
        detailCode = 100,
        message = "No such resource exists",
        developerMessage = "The url you requested does not exist (Generic 404)"
)
public class NoSuchResourceException extends RuntimeException {

}
