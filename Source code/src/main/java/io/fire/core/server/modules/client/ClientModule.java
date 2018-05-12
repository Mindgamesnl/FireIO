package io.fire.core.server.modules.client;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.objects.FireIoConnection;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientModule {

    private FireIoServer server;

    //map with all connections
    public Map<UUID, FireIoConnection> connectionMap = new ConcurrentHashMap<>();

    public ClientModule(FireIoServer s) {
        server = s;
    }

    //get client by id
    public FireIoConnection getClient(UUID connectionId) {
        //check if it exists
        if (!connectionMap.containsKey(connectionId)) return null;
        //return
        return connectionMap.get(connectionId);
    }

    //remove client and delete it's resources
    public void removeClient(UUID c) {
        connectionMap.remove(c);
    }

    //create new empty client for new connections
    public FireIoConnection registerConnection() {
        //create connection object
        FireIoConnection client = new FireIoConnection(server);
        //register it with its newly generated id
        connectionMap.put(client.getId(), client);
        //return new client
        return client;
    }

    public Collection<FireIoConnection> getAll() {
        //get and return all connections
        return connectionMap.values();
    }
}
