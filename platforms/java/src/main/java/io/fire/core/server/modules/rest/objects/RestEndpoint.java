package io.fire.core.server.modules.rest.objects;

import io.fire.core.server.modules.rest.interfaces.RestExchange;
import lombok.Getter;

public class RestEndpoint {

    @Getter private String path;
    @Getter private String[] parts;
    @Getter private Boolean hasVariable;
    @Getter private RestExchange restExchange;

    public RestEndpoint(String path, RestExchange exchange) {
        this.path = path;
        this.parts = path.split("/");
        this.hasVariable = path.contains("?");
        this.restExchange = exchange;
    }

}
