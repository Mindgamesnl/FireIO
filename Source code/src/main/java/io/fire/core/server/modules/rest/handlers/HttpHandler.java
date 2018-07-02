package io.fire.core.server.modules.rest.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.rest.RestModule;
import io.fire.core.server.modules.rest.enums.ContentType;
import io.fire.core.server.modules.rest.interfaces.RestRequest;
import io.fire.core.server.modules.rest.objects.RestEndpoint;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class HttpHandler implements com.sun.net.httpserver.HttpHandler {

    private FireIoServer server;
    private RestModule module;
    private List<RestEndpoint> endpointList = new ArrayList<>();
    //password is null by default (this means it is open for everyone)2
    @Setter private String password = null;

    public HttpHandler(FireIoServer server, RestModule restModule) {
        this.server = server;
        this.module = restModule;
    }

    public void addEndpoint(RestEndpoint restEndpoint) {
        endpointList.add(restEndpoint);
    }

    public void handle(HttpExchange httpExchange) throws IOException {

        String url = httpExchange.getRequestURI().toString();
        //check requested url

        String adress = httpExchange.getRemoteAddress().getHostName();
        //check rate limiter
        if (!module.getRateLimiter().allowed(adress)) {
            //set response to the fail auth body
            //blocked by rate limiter!
            String response = "ratelimit";
            emit(httpExchange, 200, response, ContentType.PLAINTEXT);
            //end request
            return;
        }

        //check if it is endpoint for authentication
        if (url.startsWith("/fireio/register")) {
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
                    emit(httpExchange, 200, response, ContentType.PLAINTEXT);
                    //end request
                    return;
                }
            }

            //register a new connection and get the assigned id as string
            String response = server.getClientModule().registerConnection().getId().toString();
            emit(httpExchange, 200, response, ContentType.PLAINTEXT);
        } else {
            if (url.equals("/")) {
                emit(httpExchange, 200, "{\"message\":\"FireIO java server from https://github.com/Mindgamesnl/FireIO\"}", ContentType.JSON);
                return;
            } else {
                String[] requestedParts = url.split("/");
                RestEndpoint endpoint = null;
                List<String> variables = new ArrayList<>();
                int score = 0;
                for (RestEndpoint optionalPoint : endpointList) {
                    List<String> optionalVariables = new ArrayList<>();
                    int optionalScore = 0;
                    if (!(requestedParts.length < optionalPoint.getParts().length)) {
                        if (optionalPoint.getHasVariable()) {
                            for (int i = 0; i < optionalPoint.getParts().length; i++) {
                                if (optionalPoint.getParts()[i].equals("?")) {
                                    optionalScore++;
                                    optionalVariables.add(requestedParts[i]);
                                } else {
                                    if (requestedParts[i].equals(optionalPoint.getParts()[i])) {
                                        optionalScore++;
                                    } else {
                                        optionalScore = optionalScore - 500;
                                    }
                                }
                            }
                        } else {
                            if (url.equals(optionalPoint.getPath())) {
                                optionalScore = 500;
                            }
                        }
                        if (optionalScore >= score) {
                            if (optionalScore != 0) {
                                endpoint = optionalPoint;
                                score = optionalScore;
                                variables = optionalVariables;
                            }
                        }
                    }
                }

                if (endpoint == null) {
                    emit(httpExchange, 200, "{\"message\":\"FireIO doesn't have a registered REST endpoint with the given name\"}", ContentType.JSON);
                } else {
                    List<String> finalVariables = variables;
                    String out = endpoint.getRestExchange().onRequest(new RestRequest() {
                        @Override
                        public InetSocketAddress getRequester() {
                            return httpExchange.getRemoteAddress();
                        }

                        @Override
                        public String getVariable(int key) {
                            return finalVariables.get(key);
                        }

                        @Override
                        public InputStream getRequestBody() {
                            return httpExchange.getRequestBody();
                        }

                        @Override
                        public Headers getHeaders() {
                            return httpExchange.getRequestHeaders();
                        }

                        @Override
                        public String getURL() {
                            return url;
                        }
                    });
                    if (out.contains("{")) {
                        emit(httpExchange, 200, out, ContentType.JSON);
                    } else {
                        emit(httpExchange, 200, out, ContentType.PLAINTEXT);
                    }
                }
            }
        }
    }

    private void emit(HttpExchange httpExchange, int statusCode, String response, ContentType type) throws IOException {
        httpExchange.sendResponseHeaders(statusCode, response.length());

        if (type == ContentType.JSON) {
            httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        } else {
            httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        }

        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
