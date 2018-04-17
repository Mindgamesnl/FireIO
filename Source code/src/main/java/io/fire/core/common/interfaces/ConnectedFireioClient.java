package io.fire.core.common.interfaces;

import java.util.UUID;

public interface ConnectedFireioClient {

    void send(String channel, String message);
    void send(String channel, Packet packet);
    UUID getId();

}
