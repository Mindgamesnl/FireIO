package io.fire.core.common.io.socket.interfaces;

import java.io.IOException;
import java.util.UUID;

public interface GenericClient {

    void send(String channel, Packet packet) throws IOException;
    void send(String channel, String message) throws IOException;
    void close();
    UUID getId();

}
