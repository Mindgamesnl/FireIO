package io.fire.core.server.modules.rest.interfaces;

public interface RestExchange {
    String onRequest(RestRequest req, RestResponse res);
}
