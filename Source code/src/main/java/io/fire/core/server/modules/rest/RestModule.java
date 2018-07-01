package io.fire.core.server.modules.rest;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import io.fire.core.common.ratelimiter.RateLimit;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.rest.handlers.HttpHandler;
import io.fire.core.server.modules.rest.interfaces.RestExchange;
import lombok.Getter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class RestModule {

    private HttpHandler httpHandler;
    private HttpServer httpServer;
    @Getter private RateLimit rateLimiter = new RateLimit(20, 10);

    public RestModule(FireIoServer server, int port) throws IOException {
        //http webservice for start of anonymous registration
        //create server with address
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        //create new request handler
        httpHandler = new HttpHandler(server, this);
        //create endpoint and assign it the handler
        HttpContext context = httpServer.createContext("/fireio/register");
        context.setHandler(httpHandler);
        //create thread pool for requests
        httpServer.setExecutor(Executors.newCachedThreadPool());
        //start server
        httpServer.start();
    }

    public void registerEndpoint(RestExchange restExchange) {
        HttpContext context = httpServer.createContext("/fireio/register");
        context.setHandler(httpHandler);
    }

    public void setRateLimiter(int timeout, int attempts) {
        rateLimiter.stop();
        rateLimiter = new RateLimit(timeout, attempts);
    }

    public void setPassword(String password) {
        //set a password
        //push it through to the handler
        httpHandler.setPassword(password);
    }

}
