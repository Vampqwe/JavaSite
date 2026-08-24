package com.engine.core;

import java.util.Map;

/**
 * Класс, представляющий HTTP-запрос.
 */
public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> headers;

    public HttpRequest(String method, String path, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }
}
