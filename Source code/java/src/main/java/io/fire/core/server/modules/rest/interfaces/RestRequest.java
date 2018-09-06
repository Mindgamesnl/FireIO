package io.fire.core.server.modules.rest.interfaces;

import com.sun.net.httpserver.Headers;
import io.fire.core.server.modules.rest.enums.RequestMethod;
import io.fire.core.server.modules.rest.objects.RequestBody;

import java.net.InetSocketAddress;

public interface RestRequest {

    InetSocketAddress getRequester();
    String getVariable(String name);
    RequestBody getRequestBody();
    Headers getHeaders();
    String getURL();
    RequestMethod getMethod();

}
