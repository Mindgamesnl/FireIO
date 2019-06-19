package io.fire.core.server.modules.socket.objects;

import io.fire.core.common.io.socket.interfaces.GenericClient;
import io.fire.core.common.io.socket.interfaces.Packet;
import io.fire.core.server.modules.socket.driver.SocketDriver;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
public class Client implements GenericClient {

    private SocketDriver socketDriver;

    @Override
    public void send(String channel, Packet packet) throws IOException {
        socketDriver.send(channel, packet);
    }

    @Override
    public void send(String channel, String message) throws IOException {
        socketDriver.send(channel, message);
    }

    @Override
    public void close() {
        //TODO: implement
    }

    @Override
    public UUID getId() {
        //TODO: implement
        return null;
    }
}
