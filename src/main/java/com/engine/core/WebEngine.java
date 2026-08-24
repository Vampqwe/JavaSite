package com.engine.core;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Основной класс легкого веб-движка.
 * Предоставляет базовый HTTP-сервер с возможностью расширения через маршрутизацию.
 */
public class WebEngine {
    private static final Logger logger = LoggerFactory.getLogger(WebEngine.class);
    
    private final int port;
    private final String staticRoot;
    private final ExecutorService executorService;
    private final Map<String, RequestHandler> routes;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public WebEngine(int port, String staticRoot) {
        this.port = port;
        this.staticRoot = staticRoot;
        this.executorService = Executors.newFixedThreadPool(10);
        this.routes = new HashMap<>();
        this.running = false;
    }

    /**
     * Регистрация обработчика запросов для конкретного пути.
     * Позволяет добавлять динамическую логику поверх статических файлов.
     */
    public void registerRoute(String path, RequestHandler handler) {
        routes.put(path, handler);
        logger.info("Зарегистрирован маршрут: {}", path);
    }

    /**
     * Запуск сервера.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            logger.info("Веб-движок запущен на порту {}", port);
            logger.info("Корневая директория статических файлов: {}", staticRoot);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        logger.error("Ошибка при принятии соединения", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка запуска сервера", e);
        } finally {
            stop();
        }
    }

    /**
     * Остановка сервера.
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            executorService.shutdown();
            logger.info("Веб-движок остановлен");
        } catch (IOException e) {
            logger.error("Ошибка при остановке сервера", e);
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                return;
            }

            String method = parts[0];
            String path = parts[1];

            // Чтение заголовков (можно расширить для парсинга)
            Map<String, String> headers = new HashMap<>();
            String headerLine;
            while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                String[] headerParts = headerLine.split(": ", 2);
                if (headerParts.length == 2) {
                    headers.put(headerParts[0], headerParts[1]);
                }
            }

            logger.info("{} {}", method, path);

            // Проверка зарегистрированных маршрутов
            if (routes.containsKey(path)) {
                RequestHandler handler = routes.get(path);
                HttpResponse response = handler.handle(new HttpRequest(method, path, headers));
                sendResponse(out, response);
            } else {
                // Обработка статических файлов
                serveStaticFile(path, out, clientSocket);
            }

        } catch (IOException e) {
            logger.error("Ошибка обработки клиента", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Игнорируем
            }
        }
    }

    private void serveStaticFile(String path, PrintWriter out, Socket clientSocket) {
        if ("/".equals(path)) {
            path = "/index.html";
        }

        Path filePath = Paths.get(staticRoot, path);
        
        try {
            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                String contentType = getContentType(path);
                byte[] content = Files.readAllBytes(filePath);
                
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: " + contentType);
                out.println("Content-Length: " + content.length);
                out.println();
                out.flush();
                
                // Отправка бинарных данных через сокет
                try (OutputStream os = clientSocket.getOutputStream()) {
                    os.write(content);
                    os.flush();
                }
                logger.info("Отправлен файл: {}", path);
            } else {
                sendNotFound(out);
            }
        } catch (IOException e) {
            sendServerError(out);
        }
    }

    private void sendResponse(PrintWriter out, HttpResponse response) {
        out.println("HTTP/1.1 " + response.getStatusCode());
        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            out.println(header.getKey() + ": " + header.getValue());
        }
        out.println();
        out.print(response.getBody());
        out.flush();
    }

    private void sendNotFound(PrintWriter out) {
        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<html><body><h1>404 - Файл не найден</h1></body></html>");
        out.flush();
    }

    private void sendServerError(PrintWriter out) {
        out.println("HTTP/1.1 500 Internal Server Error");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<html><body><h1>500 - Внутренняя ошибка сервера</h1></body></html>");
        out.flush();
    }

    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".ico")) return "image/x-icon";
        if (path.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }

    public static void main(String[] args) {
        int port = 8080;
        String staticRoot = "src/main/resources/static";

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Неверный формат порта. Используется порт по умолчанию: 8080");
            }
        }

        WebEngine engine = new WebEngine(port, staticRoot);

        // Пример регистрации динамического маршрута (для будущего расширения)
        engine.registerRoute("/api/hello", (request) -> {
            return new HttpResponse("200 OK", 
                Map.of("Content-Type", "application/json"), 
                "{\"message\": \"Привет от легкого движка!\"}");
        });

        // Добавляем хук для остановки по Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(engine::stop));

        engine.start();
    }
}
