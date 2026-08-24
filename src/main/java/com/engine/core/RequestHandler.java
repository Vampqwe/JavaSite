package com.engine.core;

import java.util.Map;

/**
 * Интерфейс обработчика HTTP-запросов.
 * Позволяет добавлять пользовательскую логику обработки запросов.
 */
@FunctionalInterface
public interface RequestHandler {
    HttpResponse handle(HttpRequest request);
}
