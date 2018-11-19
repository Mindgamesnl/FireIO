package io.fire.core.common.io.api.processors;

import io.fire.core.common.io.api.api.IncomingRequest;
import io.fire.core.common.io.api.api.ResponseSettings;
import io.fire.core.common.io.api.request.HttpEndpoint;
import io.fire.core.common.io.api.request.HttpInteraction;
import io.fire.core.common.io.api.request.PendingRequest;
import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.io.http.enums.HttpStatusCode;
import io.fire.core.common.io.http.objects.ConnectionInfo;
import io.fire.core.common.io.http.objects.HttpContent;
import io.fire.core.common.objects.VersionInfo;
import io.fire.core.server.modules.http.HttpModule;

import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestProcessor {

    @Setter private String password = null;
    private HttpModule module;
    private VersionInfo versionInfo = new VersionInfo();

    private List<HttpEndpoint> endpointList = new ArrayList<>();
    private Map<String, HttpEndpoint> cachedEndpoints = new HashMap<>();


    /**
     * Setup the HTTP module with the HTTP-PRE-PROCESSORS and default api endpoints
     *
     * @param httpModule
     */
    public HttpRequestProcessor(HttpModule httpModule) {
        this.module = httpModule;
        //registration endpoint
        HttpInteraction interaction = ((request, settings) -> {
            //a new http request received!
            //split the url at the password tag
            if (password != null) {
                if (request.getVariable("password").equals("")) {
                    return "fail-auth";
                }
                if (!request.getVariable("password").equals(password)) {
                    return "fail-auth";
                }
            }
            //check if the password (on the server side) is not null
            //this means that we need to check authentication
            String out = module.getMain().getClientModule().registerConnection().getId().toString();
            out += "INFO:" + new VersionInfo().toString();
            return out;
        });
        registerHandler("/fireio/register", interaction);
        registerHandler("/fireio/register/?password", interaction);
    }


    /**
     * Register a endpoint with a path and a callback to fire
     *
     * @param path
     * @param interaction
     */
    public void registerHandler(String path, HttpInteraction interaction) {
        endpointList.add(new HttpEndpoint(path, interaction));
    }


    /**
     * Handle a pending request.
     * This parses the HTTP request, does a lookup for the handler, executes the handler
     * set's up the response and then emits the response to the client
     *
     * @param pendingRequest
     * @throws IOException
     */
    public void handle(PendingRequest pendingRequest) throws IOException {
        HttpContent response = new HttpContent();
        HttpContent request = pendingRequest.getHeaders();

        //ip data
        //variables we will use for setting the ip
        ConnectionInfo connectionInfo = null;

        //configure ip adress for proxxy
        if (request.getRancherActiveProxyContent() != null) {
            connectionInfo = new ConnectionInfo(request.getRancherActiveProxyContent().getForwardedPort(),
                    request.getRancherActiveProxyContent().getRealIp(),
                    request.getRancherActiveProxyContent().isForwardedSsl(),
                    request.getRancherActiveProxyContent().getForwardedProtocol(),
                    true);
        } else {
            connectionInfo = new ConnectionInfo(module.getMain().getPort(),
                    pendingRequest.getSocketChannel().socket().getInetAddress().getHostAddress(),
                    false,
                    null,
                    false);
        }

        //data
        String url = request.getUrl();

        //default headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");

        //check rate limiter
        if (!module.getRateLimiter().allowed(connectionInfo.getIpAddress())) {
            //set response to the fail auth body
            //blocked by rate limiter!
            response.setOpcode(HttpContentType.TEXT, HttpStatusCode.C_403);
            response.setBody("ratelimit");
            pendingRequest.finish(response);
            return;
        }

        Map<String, String> variables = new HashMap<>();
        HttpEndpoint handler = null;

        if (url.equals("/")) {
            //root
            response.setOpcode(HttpContentType.HTML, HttpStatusCode.C_200);
            response.setBody(module.getHttpResources().get("welcome.html").replace("{{version}}", "<b>V" + versionInfo.getCoreVersion() + "</b>").replace("{{protocol-version}}", "<b>V" + versionInfo.getProtocolVersion() + "</b>"));
            pendingRequest.finish(response);
            return;
        } else {

            //cache
            HttpEndpoint cachedResponse = cachedEndpoints.get(url);
            if (cachedResponse != null) {
                accept(pendingRequest, request, url, variables, cachedResponse, connectionInfo);
                return;
            }

            String[] requestedParts = url.split("/");
            int score = 0;
            for (HttpEndpoint optionalPoint : endpointList) {
                Map<String, String> optionalVariables = new HashMap<>();
                int optionalScore = 0;
                if (!(requestedParts.length < optionalPoint.getParts().length)) {
                    if (optionalPoint.getHasVariable()) {
                        for (int i = 0; i < optionalPoint.getParts().length; i++) {
                            if (optionalPoint.getParts()[i].startsWith("?")) {
                                optionalScore++;
                                String v = optionalPoint.getParts()[i];
                                v = v.replace("?", "");
                                optionalVariables.put(v, requestedParts[i]);
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
                            handler = optionalPoint;
                            score = optionalScore;
                            variables = optionalVariables;
                        }
                    }
                }
            }
        }

        if (handler == null) {
            //404
            response.setOpcode(HttpContentType.HTML, HttpStatusCode.C_404);
            response.setBody(module.getHttpResources().get("404.html"));
            pendingRequest.finish(response);
        } else {
            //accept endpoint
            accept(pendingRequest, request, url, variables, handler, connectionInfo);
            //save in cache
            if (variables.size() == 0) cachedEndpoints.put(url, handler);
        }
    }


    /**
     * Accept PendingRequest
     * This function finishes the request, makes it ready for reading by the client and then sends it over the socket.
     *
     * @param pendingRequest
     * @param request
     * @param url
     * @param variables
     * @param handler
     * @param connectionInfo
     */
    private void accept(PendingRequest pendingRequest, HttpContent request, String url, Map<String, String> variables, HttpEndpoint handler, ConnectionInfo connectionInfo) {
        //handle endpoint
        IncomingRequest incomingRequest = new IncomingRequest(request, url, connectionInfo, variables);
        ResponseSettings responseSettings = new ResponseSettings();
        String body = "[[body]]";
        boolean successful = true;
        try {
            body = handler.getHandler().complete(incomingRequest, responseSettings);
        } catch (Exception e) {
            successful = false;
            body = module.getHttpResources().get("500.html").replace("{{stacktrace-message}}", e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        HttpContent output = new HttpContent();
        responseSettings.getHeaders().forEach((k, v) -> output.setHeader(k, v));

        if (!successful) {
            output.setOpcode(HttpContentType.HTML, HttpStatusCode.C_500);
        } else {
            output.setOpcode(responseSettings.getContent(), responseSettings.getStatusCode());
        }

        output.setBody(body);

        pendingRequest.finish(output);
    }

}
