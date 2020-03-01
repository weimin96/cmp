package com.wiblog.cmp.client.common;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

/**
 * @author pwm
 * @date 2020/2/12
 */
public class HttpResponse<T> {
    private final int statusCode;
    private final T entity;
    private final Map<String, String> headers;
    private final URI location;

    public HttpResponse(int statusCode, T entity) {
        this.statusCode = statusCode;
        this.entity = entity;
        this.headers = null;
        this.location = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public URI getLocation() {
        return location;
    }

    public Map<String, String> getHeaders() {
        return headers == null ? Collections.<String, String>emptyMap() : headers;
    }

    public T getEntity() {
        return entity;
    }

    public static HttpResponse<Void> status(int status) {
        return new HttpResponse<>(status, null);
    }



}
