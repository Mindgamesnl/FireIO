package io.fire.core.server.modules.http;

import io.fire.core.common.io.http.enums.HttpStatusCode;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.http.interfaces.Middleware;
import io.fire.core.server.modules.http.interfaces.RouteHandler;
import io.fire.core.server.modules.http.objects.*;
import io.fire.core.server.modules.http.routes.FileStatusRoute;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpProvider {

    @Getter private List<Route> routes = new ArrayList<>();
    @Getter private Map<String, Middleware> middleware = new HashMap<>();

    public HttpProvider(FireIoServer main) {

    }

    public void addMiddleware(String name, Middleware createdMiddleware) {
        middleware.put(name, createdMiddleware);
    }

    public void registerRoute(String route, RouteHandler handler) {
        if (!route.startsWith("/")) route = "/" + route;
        routes.add(new Route(route, handler));
    }

    public ExecutableRoute getRoute(String url) {
        String[] requestParts = url.split("/");
        for (Route handler : routes) {
            if (requestParts.length != handler.getEndpoint().length) continue;
            int segmentId = 0;
            boolean isSimilar = true;
            Map<String, String> variables = new HashMap<>();

            for (String segment : handler.getEndpoint()) {
                if (segment.startsWith("?")) {
                    variables.put(segment.replace("?", ""), requestParts[segmentId]);
                } else if (!segment.equals(requestParts[segmentId])) {
                    isSimilar = false;
                    break;
                }
                segmentId++;
            }

            if (isSimilar) return new ExecutableRoute(variables, handler);
        }
        return new ExecutableRoute(null, new Route(null, new FileStatusRoute(HttpStatusCode.C_404, "404.html").setReplacement("{route}", url)));
    }

}
