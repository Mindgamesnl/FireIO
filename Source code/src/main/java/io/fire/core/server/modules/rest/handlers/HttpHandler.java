package io.fire.core.server.modules.rest.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.fire.core.server.FireIoServer;
import lombok.Setter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpHandler implements com.sun.net.httpserver.HttpHandler {

    private FireIoServer server;
    @Setter private String password = null;
    private Map<String, Date> requestHistory = new ConcurrentHashMap<>();

    public HttpHandler(FireIoServer server) {
        this.server = server;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        Date now = new Date();
        //clean old
        for (String h : requestHistory.keySet()) {
            if (h != httpExchange.getRemoteAddress().getHostName()) {
                if (diff(requestHistory.get(httpExchange.getRemoteAddress().getHostName()), now) > 2) {
                    requestHistory.remove(h);
                }
            }
        }

        //check rate limit
        if (requestHistory.containsKey(httpExchange.getRemoteAddress().getHostName())) {
            if (!(diff(requestHistory.get(httpExchange.getRemoteAddress().getHostName()), now) > 2)) {
                requestHistory.put(httpExchange.getRemoteAddress().getHostName(), now);
                String response = "ratelimit";
                httpExchange.sendResponseHeaders(200, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
        }

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

        requestHistory.put(httpExchange.getRemoteAddress().getHostName(), now);

        String response = server.getClientModule().registerConnection().getId().toString();
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public int diff(Date a, Date b) {
        int optiona = (a.getSeconds() - b.getSeconds());
        int optionb = (b.getSeconds() - a.getSeconds());

        if (optiona > 0) return optiona;
        if (optionb > 0) return optionb;
        return 0;
    }
}
