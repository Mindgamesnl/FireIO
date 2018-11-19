package io.fire.core.server.modules.http;

import io.fire.core.common.io.api.processors.HttpRequestProcessor;
import io.fire.core.common.io.http.objects.HttpResources;
import io.fire.core.common.ratelimiter.RateLimit;
import io.fire.core.server.FireIoServer;

import lombok.Getter;

public class HttpModule {

    @Getter private RateLimit rateLimiter = new RateLimit(20, 10);
    @Getter private FireIoServer main;
    @Getter private HttpRequestProcessor httpRequestProcessor;
    @Getter private HttpResources httpResources = new HttpResources();


    /**
     * Setup the http module based on the server instance
     *
     * @param server
     */
    public HttpModule(FireIoServer server) {
        this.main = server;
        httpRequestProcessor = new HttpRequestProcessor(this);
    }


    /**
     * set or change the rate limiter settings
     * this overwrites the current or default settings
     *
     * @param timeout
     * @param attempts
     */
    public void setRateLimiter(int timeout, int attempts) {
        rateLimiter.stop();
        rateLimiter = new RateLimit(timeout, attempts);
    }


    /**
     * Set the password for authentication
     * not set by default, can be used to overtire the current password
     *
     * @param password
     */
    public void setPassword(String password) {
        //set a password
        //push it through to the handler
        httpRequestProcessor.setPassword(password);
    }

}
