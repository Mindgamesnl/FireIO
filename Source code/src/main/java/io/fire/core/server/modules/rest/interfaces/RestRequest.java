package io.fire.core.server.modules.rest.interfaces;

import com.sun.net.httpserver.Headers;

import java.io.InputStream;
import java.net.InetSocketAddress;

public interface RestRequest {

    InetSocketAddress getRequester();
    String getVariable(String name);
    InputStream getRequestBody();
    Headers getHeaders();
    String getURL();

}
