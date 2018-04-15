package io.fire.core.server;

import io.fire.core.common.events.enums.Event;
import io.fire.core.common.events.interfaces.Listener;
import io.fire.core.common.events.EventHandler;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.server.modules.client.ClientModule;
import io.fire.core.server.modules.client.objects.FireIoConnection;
import io.fire.core.server.modules.client.superclasses.Client;
import io.fire.core.server.modules.rest.RestModule;
import io.fire.core.server.modules.socket.SocketModule;

import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FireIoServer {

    private int port;

    //modules
    @Getter private SocketModule socketModule;
    @Getter private RestModule restModule;
    @Getter private ClientModule clientModule;
    @Getter private EventHandler eventHandler;

    public FireIoServer(int port) throws IOException {
        this.port = port;
        eventHandler = new EventHandler();
        clientModule = new ClientModule(this);
        restModule = new RestModule(this, (port + 1));
        socketModule = new SocketModule(this, port);
    }

    public FireIoServer on(Event e, Listener r) {
        eventHandler.on(e, r);
        return this;
    }

    public Client getClient(UUID id) {
        return clientModule.getClient(id);
    }

    public Client getClientByTag(String key, String value) {
        for (FireIoConnection client : clientModule.getAll()) {
            if (client.getInfo().getArguments().containsKey(key)
                    && client.getInfo().getArguments().get(key).equals(value)) return client;
        }
        return null;
    }

    public List<Client> getClientsByTag(String key, String value) {
        List<Client> clients = new ArrayList<>();
        for (FireIoConnection client : clientModule.getAll()) {
            if (client.getInfo().getArguments().containsKey(key)
                    && client.getInfo().getArguments().get(key).equals(value)) clients.add(client);
        }
        return clients;
    }

    public FireIoServer setPassword(String password) {
        restModule.setPassword(password);
        return this;
    }

    public FireIoServer broadcast(String channel, Packet packet) {
        for (Client c : clientModule.connectionMap.values()) {
            c.send(channel, packet);
        }
        return this;
    }

    public FireIoServer broadcast(String channel, String message) {
        for (Client c : clientModule.connectionMap.values()) {
            c.send(channel, message);
        }
        return this;
    }

    public FireIoServer on(String e, Listener r) {
        eventHandler.on(e, r);
        return this;
    }

}
