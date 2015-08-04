package com.erigir.wrench.ape.exception;

import com.erigir.wrench.ape.http.ApeException;

import java.util.List;

/**
 * Created by chrweiss on 6/28/14.
 */
@ApeException(
        httpStatusCode = 404,
        detailCode = 107,
        message = "Your application is currently misconfigured",
        developerMessage = "You requested a version of the API that does not currently exist",
        detailObjectPropertyName = "validVersions"
)
public class NoSuchVersionException extends RuntimeException {
    private List<Integer> validVersions;

    public NoSuchVersionException(List<Integer> validVersions) {
        this.validVersions = validVersions;
    }

    public List<Integer> getValidVersions() {
        return validVersions;
    }
}
