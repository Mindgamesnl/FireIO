package io.fire.core.server.modules.socket.managers;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.ClientModule;
import io.fire.core.server.modules.socket.handlers.SocketClientHandler;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {

    private FireIoServer server;
    public Map<SocketAddress, SocketClientHandler> references = new ConcurrentHashMap<>();

    public ClientManager(FireIoServer server) {
        this.server = server;
    }

}
