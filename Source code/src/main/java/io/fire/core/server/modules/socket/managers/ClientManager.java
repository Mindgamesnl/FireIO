package io.fire.core.server.modules.socket.managers;

import io.fire.core.server.modules.socket.handlers.SocketClientHandler;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {

    public Map<SocketAddress, SocketClientHandler> references = new ConcurrentHashMap<>();

    public ClientManager() {}

}
