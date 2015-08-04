package com.erigir.wrench.ape.resttemplate;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

import java.io.IOException;

/**
 * A simple client interceptor for setting headers in a rest template
 * Created by cweiss on 7/18/15.
 */
public class HeaderSettingClientInterceptor implements ClientHttpRequestInterceptor {

    /** */
    private final String headerName;

    /** */
    private final String headerValue;

    public HeaderSettingClientInterceptor(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpRequest wrapper = new HttpRequestWrapper(request);
        wrapper.getHeaders().set(headerName, headerValue);
        return execution.execute(wrapper, body);
    }
}
