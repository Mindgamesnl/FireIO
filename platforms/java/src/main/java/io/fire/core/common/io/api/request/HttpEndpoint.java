package io.fire.core.common.io.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class HttpEndpoint {

    private String path;
    private String[] parts;
    private Boolean hasVariable;
    private HttpInteraction handler;

    public HttpEndpoint(String path, HttpInteraction exchange) {
        this.path = path;
        this.parts = path.split("/");
        this.hasVariable = path.contains("?");
        this.handler = exchange;
    }

}
