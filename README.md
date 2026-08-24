# Легкий веб-движок на Java

Простой и расширяемый веб-движок, написанный на Java с нуля.

## Возможности

✅ **Базовые функции:**
- HTTP-сервер на чистых Java Socket
- Обслуживание статических файлов (HTML, CSS, JS, изображения)
- Маршрутизация запросов (REST API)
- Многопоточная обработка запросов (ThreadPool)
- Логгирование событий

🔧 **Возможности расширения ("утяжеления"):**
- Добавление поддержки сессий и cookie
- Интеграция шаблонизаторов (Thymeleaf, FreeMarker)
- Подключение баз данных (JDBC, JPA/Hibernate)
- Система аутентификации и авторизации
- WebSocket для real-time общения
- Кэширование и оптимизация производительности
- Поддержка аннотаций для контроллеров
- Внедрение зависимостей (DI)

## Структура проекта

```
light-web-engine/
├── src/main/java/com/engine/core/
│   ├── WebEngine.java        # Основной класс сервера
│   ├── RequestHandler.java   # Интерфейс обработчика запросов
│   ├── HttpRequest.java      # Модель HTTP-запроса
│   └── HttpResponse.java     # Модель HTTP-ответа
├── src/main/resources/static/
│   └── index.html            # Стартовая страница
├── pom.xml                   # Maven конфигурация
└── README.md                 # Этот файл
```

## Быстрый старт

### Требования
- Java 17 или выше
- Maven 3.6+

### Запуск

```bash
# Сборка проекта
mvn clean package

# Запуск на порту 8080 (по умолчанию)
java -jar target/light-web-engine-1.0-SNAPSHOT.jar

# Запуск на другом порту
java -jar target/light-web-engine-1.0-SNAPSHOT.jar 9000
```

### Проверка работы

Откройте браузер и перейдите по адресу: `http://localhost:8080`

Проверьте API: `http://localhost:8080/api/hello`

## Пример расширения движка

Добавление нового маршрута:

```java
WebEngine engine = new WebEngine(8080, "static");

// Простой REST endpoint
engine.registerRoute("/api/users", (request) -> {
    return new HttpResponse("200 OK", 
        Map.of("Content-Type", "application/json"), 
        "{\"users\": [\"Alice\", \"Bob\"]}");
});

// Обработка POST запросов
engine.registerRoute("/api/data", (request) -> {
    if ("POST".equals(request.getMethod())) {
        // Логика обработки POST
        return new HttpResponse("201 Created", 
            Map.of("Content-Type", "application/json"), 
            "{\"status\": \"created\"}");
    }
    return new HttpResponse("405 Method Not Allowed", 
        Map.of("Content-Type", "text/plain"), 
        "Method not allowed");
});

engine.start();
```

## Архитектура

Движок построен по модульному принципу:

1. **WebEngine** - ядро сервера, управляет соединениями и маршрутизацией
2. **RequestHandler** - функциональный интерфейс для обработки запросов
3. **HttpRequest/HttpResponse** - простые модели данных

Такая архитектура позволяет легко добавлять новые функции без переписывания ядра.

## Лицензия

MIT License
