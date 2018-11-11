package io.fire.core.common.io.api.processors;

import io.fire.core.common.io.api.api.IncomingRequest;
import io.fire.core.common.io.api.api.ResponseSettings;
import io.fire.core.common.io.api.request.HttpEndpoint;
import io.fire.core.common.io.api.request.HttpInteraction;
import io.fire.core.common.io.api.request.PendingRequest;
import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.io.http.enums.HttpStatusCode;
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

    public void registerHandler(String path, HttpInteraction interaction) {
        endpointList.add(new HttpEndpoint(path, interaction));
    }

    public void handle(PendingRequest pendingRequest) throws IOException {
        HttpContent response = new HttpContent();
        HttpContent request = pendingRequest.getHeaders();

        //data
        String url = request.getUrl();

        //default headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");

        //check rate limiter
        if (!module.getRateLimiter().allowed(pendingRequest.getSocketChannel().socket().getInetAddress().getHostName())) {
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
            response.setBody(module.getHttpResources().get("welcome.html").replace("{{version}}", versionInfo.getCoreVersion() + "C").replace("{{protocol-version}}", versionInfo.getProtocolVersion() + "P"));;
            pendingRequest.finish(response);
            return;
        } else {
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
            //handle endpoint
            IncomingRequest incomingRequest = new IncomingRequest(request, url, pendingRequest.getSocketChannel().socket(), variables);
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

}
