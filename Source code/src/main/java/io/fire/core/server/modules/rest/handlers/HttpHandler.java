package io.fire.core.server.modules.rest.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.fire.core.server.FireIoServer;
import lombok.Setter;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpHandler implements com.sun.net.httpserver.HttpHandler {

    private FireIoServer server;
    @Setter private String password = null;

    public HttpHandler(FireIoServer server) {
        this.server = server;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        //check password
        String requestedPassword = httpExchange.getRequestURI().getRawQuery().split("p=")[1];
        if (password != null) {
            if (!requestedPassword.equals(password)) {
                String response = "fail-auth";
                httpExchange.sendResponseHeaders(200, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
        }

        String response = server.getClientModule().registerConnection().getId().toString();
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
