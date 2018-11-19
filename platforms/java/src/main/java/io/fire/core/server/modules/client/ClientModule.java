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


    /**
     * Get a client by ID
     * returns NULL if the id is not valid
     *
     * @param connectionId
     * @return
     */
    public FireIoConnection getClient(UUID connectionId) {
        if (connectionId == null) return null;
        //check if it exists
        if (!connectionMap.containsKey(connectionId)) return null;
        //return
        return connectionMap.get(connectionId);
    }


    /**
     * Remove a client and delete its resources
     * Prevents memory leaks and unused objects hanging around
     *
     * @param c
     */
    public void removeClient(UUID c) {
        connectionMap.remove(c);
    }


    /**
     * Create a new empty client
     * Ready to setup for new incoming connections
     *
     * @return
     */
    public FireIoConnection registerConnection() {
        //create connection object
        FireIoConnection client = new FireIoConnection(server);
        //register it with its newly generated id
        connectionMap.put(client.getId(), client);
        //return new client
        return client;
    }


    /**
     * Get all connected clients
     *
     * @return
     */
    public Collection<FireIoConnection> getAll() {
        //get and return all connections
        return connectionMap.values();
    }
}
