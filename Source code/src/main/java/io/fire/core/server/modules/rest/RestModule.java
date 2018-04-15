package io.fire.core.server.modules.rest;

import com.sun.net.httpserver.HttpServer;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.rest.handlers.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class RestModule {

    private HttpHandler httpHandler;

    public RestModule(FireIoServer server, int port) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpHandler = new HttpHandler(server);
        httpServer.createContext("/fireio/register", httpHandler);
        httpServer.setExecutor(null);
        httpServer.start();
    }

    public void setPassword(String password) {
        httpHandler.setPassword(password);
    }

}
