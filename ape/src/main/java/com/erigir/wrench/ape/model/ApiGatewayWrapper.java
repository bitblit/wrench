package com.erigir.wrench.ape.model;

import java.util.List;
import java.util.Map;

/**
 * AWS ApiGateway is a cool feature allowing you to treat http requests as triggers for AWS lambda functions -
 * the only problem is that they hide most of the HTTP request from you!  By default the body of the request is
 * passed as the body of the lambda request - but what if you need the rest of the request?
 *
 * You can configure it to create a new body with that stuff in it, but then that decoration screws with the
 * rest of your api.  This object is to swallow the decoration
 *
 * This object is to act as
 * Created by cweiss1271 on 12/10/15.
 */
public class ApiGatewayWrapper<T> {
    private Map<String,Object> headers;
    private Map<String,List<String>> parameters;
    private T bodyObject;

    private String url;


    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, List<String>> parameters) {
        this.parameters = parameters;
    }

    public T getBodyObject() {
        return bodyObject;
    }

    public void setBodyObject(T bodyObject) {
        this.bodyObject = bodyObject;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
