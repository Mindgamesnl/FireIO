package io.fire.core.common.interfaces;

import java.util.UUID;

public interface ConnectedFireioClient {

    public void send(String channel, String message);
    public void send(String channel, Packet packet);
    public UUID getId();

}
