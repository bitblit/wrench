package com.erigir.wrench.ape.exception;

import com.erigir.wrench.ape.http.ApeException;

/**
 * Created by chrweiss on 6/28/14.
 */
@ApeException(
    httpStatusCode = 403,
    detailCode = 102,
    message = "You don't have the authority to perform that action",
    developerMessage = "The API key you supplied doesn't have sufficient privileges to do that"
)
public class InsufficientPrivilegeException extends RuntimeException {

}
