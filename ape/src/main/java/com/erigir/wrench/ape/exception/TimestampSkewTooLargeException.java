package com.erigir.wrench.ape.exception;

import com.erigir.wrench.ape.http.ApeException;

/**
 * Created by chrweiss on 6/28/14.
 */
@ApeException(
        httpStatusCode = 403,
        detailCode = 109,
        message = "The timestamp in the X-SCRIBE-TIMESTAMP header is outside of the valid range",
        developerMessage = "The timestamp in the X-SCRIBE-TIMESTAMP header is too far out of skew with our server - check your servers clock and the details",
        detailObjectPropertyName = "skewDescription"
)
public class TimestampSkewTooLargeException extends RuntimeException {
    private long skew;
    private long max;

    public TimestampSkewTooLargeException(long skew, long max) {
        this.skew = skew;
        this.max = max;
    }

    public String getSkewDescription() {
        return "Found skew of " + skew + " ms when only " + max + " are allowed";
    }
}
