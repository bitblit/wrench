package com.erigir.wrench.aws.cloudfront.logparser;

/**
 * Created by chrweiss on 3/16/15.
 */
public enum CloudFrontAccessLogField {
    DATE,
    TIME,
    EDGE_LOCATION,
    SC_BYTES,

    IP,
    METHOD,
    CS_HOST,
    URI_STEM,
    STATUS,
    REFERER,
    USER_AGENT,
    URI_QUERY,
    COOKIE,

    EDGE_RESULT_TYPE,
    EDGE_REQUEST_ID,
    HOST_HEADER,
    PROTOCOL,
    CS_BYTES,

    RAW,
    USER_AGENT_DECODED,


    TIME_TAKEN;

}
