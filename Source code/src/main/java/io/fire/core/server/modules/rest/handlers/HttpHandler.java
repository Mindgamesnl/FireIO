package io.fire.core.server.modules.rest.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.rest.RestModule;
import lombok.Setter;

import java.io.IOException;
import java.io.OutputStream;

public class HttpHandler implements com.sun.net.httpserver.HttpHandler {

    private FireIoServer server;
    private RestModule module;
    //password is null by default (this means it is open for everyone)2
    @Setter private String password = null;

    public HttpHandler(FireIoServer server, RestModule restModule) {
        this.server = server;
        this.module = restModule;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        String adress = httpExchange.getRemoteAddress().getHostName();
        //check rate limiter
        if (!module.getRateLimiter().allowed(adress)) {
            //set response to the fail auth body
            //blocked by rate limiter!
            String response = "ratelimit";
            //create response with status code 200 (OK) and the length of the result
            httpExchange.sendResponseHeaders(200, response.length());
            //create output stream
            OutputStream os = httpExchange.getResponseBody();
            //write stream (send to client)
            os.write(response.getBytes());
            //close stream
            os.close();
            //end request
            return;
        }

        //a new http request received!
        //split the url at the password tag
        String requestedPassword = httpExchange.getRequestURI().getRawQuery().split("p=")[1];
        //check if the password (on the server side) is not null
        //this means that we need to check authentication
        if (password != null) {
            //check if the password in the request does not equal the local password
            if (!requestedPassword.equals(password)) {
                //set response to the fail auth body
                String response = "fail-auth";
                //create response with status code 200 (OK) and the length of the result
                httpExchange.sendResponseHeaders(200, response.length());
                //create output stream
                OutputStream os = httpExchange.getResponseBody();
                //write stream (send to client)
                os.write(response.getBytes());
                //close stream
                os.close();
                //end request
                return;
            }
        }

        //register a new connection and get the assigned id as string
        String response = server.getClientModule().registerConnection().getId().toString();
        //set the response code to 200 (OK) with the length of the UUID
        httpExchange.sendResponseHeaders(200, response.length());
        //create output stream
        OutputStream os = httpExchange.getResponseBody();
        //write (send) the id, when the client receives the id's it tries to connect and authenticate with the id over socket
        os.write(response.getBytes());
        //close and finish connection
        os.close();
    }
}
