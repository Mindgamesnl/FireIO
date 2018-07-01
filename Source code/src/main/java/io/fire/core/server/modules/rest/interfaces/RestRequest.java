package io.fire.core.server.modules.rest.interfaces;

import java.net.InetSocketAddress;

public interface RestRequest {

    InetSocketAddress getRequester();
    String getVariable(int index);

}
