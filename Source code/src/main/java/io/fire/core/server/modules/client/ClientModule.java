package io.fire.core.server.modules.client;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.objects.FireIoConnection;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientModule {

    private FireIoServer server;

    public Map<UUID, FireIoConnection> connectionMap = new ConcurrentHashMap<>();

    public ClientModule(FireIoServer s) {
        server = s;
    }

    public FireIoConnection getClient(UUID connectionId) {
        if (!connectionMap.containsKey(connectionId)) return null;
        if (!connectionMap.containsKey(connectionId)) return null;
        return connectionMap.get(connectionId);
    }

    public void removeClient(UUID c) {
        connectionMap.remove(c);
    }

    public FireIoConnection registerConnection() {
        FireIoConnection client = new FireIoConnection(server);
        connectionMap.put(client.getId(), client);
        return client;
    }

    public Collection<FireIoConnection> getAll() {
        return connectionMap.values();
    }
}
