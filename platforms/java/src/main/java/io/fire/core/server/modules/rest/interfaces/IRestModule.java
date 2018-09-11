package io.fire.core.server.modules.rest.interfaces;

import io.fire.core.server.modules.rest.objects.RestEndpoint;

public interface IRestModule {

    void setRateLimiter(int timeout, int attempts);
    void addEndpoint(RestEndpoint restEndpoint);
    void setDefault(RestEndpoint exchange);
    void setPassword(String password);

}
