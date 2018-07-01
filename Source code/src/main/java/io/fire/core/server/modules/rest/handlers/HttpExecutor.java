package io.fire.core.server.modules.rest.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class HttpExecutor implements com.sun.net.httpserver.HttpHandler {

    private String path;

    public HttpExecutor(String path) {
        this.path = path;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

    }

    private void emit(HttpExchange httpExchange, int statusCode, String response) throws IOException {
        httpExchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}