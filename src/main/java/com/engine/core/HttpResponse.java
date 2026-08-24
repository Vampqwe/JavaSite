package com.engine.core;

import java.util.Map;

/**
 * Класс, представляющий HTTP-ответ.
 */
public class HttpResponse {
    private final String statusCode;
    private final Map<String, String> headers;
    private final String body;

    public HttpResponse(String statusCode, Map<String, String> headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
