package io.fire.core.server.modules.rest.interfaces;

import java.net.InetAddress;

public interface RestRequest {

    InetAddress getRequester();
    String getVariable(String key);

}
