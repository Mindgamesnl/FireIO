package io.fire.core.server.modules.http.objects;

import io.fire.core.server.modules.http.interfaces.RouteHandler;
import lombok.Getter;

public class Route {

    @Getter private String[] endpoint;
    @Getter private RouteHandler    routeHandler;

    public  Route(String url, RouteHandler routeHandler) {
        if (url == null) url = "/";
        this.routeHandler = routeHandler;
        this.endpoint = url.split("/");
    }

}
