package io.fire.core.common.interfaces;

import java.util.UUID;

public interface ConnectedFireioClient {

    //An interface to house all main api functions for a connection, this interface is commonly used in the server and client

    void send(String channel, String message);
    void send(String channel, Packet packet);
    UUID getId();

}
