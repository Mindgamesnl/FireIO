package io.fire.core.server;

import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.EventHandler;
import io.fire.core.common.eventmanager.interfaces.EventPayload;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.PoolHolder;
import io.fire.core.common.io.api.request.HttpInteraction;
import io.fire.core.common.objects.ThreadPool;
import io.fire.core.server.modules.balancingmodule.BalancingModule;
import io.fire.core.server.modules.balancingmodule.objects.BalancerConfiguration;
import io.fire.core.server.modules.client.ClientModule;
import io.fire.core.server.modules.client.objects.FireIoConnection;
import io.fire.core.server.modules.client.superclasses.Client;
import io.fire.core.server.modules.request.RequestModule;
import io.fire.core.server.modules.request.interfaces.RequestExecutor;
import io.fire.core.server.modules.http.HttpModule;
import io.fire.core.server.modules.socket.SocketModule;

import io.fire.core.server.modules.socket.enums.BlockedProtocol;
import lombok.Getter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FireIoServer implements PoolHolder {

    //modules
    @Getter private SocketModule socketModule;
    @Getter private HttpModule httpModule;
    @Getter private ClientModule clientModule;
    @Getter private EventHandler eventHandler;
    @Getter private RequestModule requestModule;
    @Getter private BalancingModule balancingModule;
    @Getter private int port = -1;
    private ThreadPool threadPool = new ThreadPool();

    public FireIoServer(int port) throws IOException {
        this.port = port;
        eventHandler = new EventHandler();
        clientModule = new ClientModule(this);
        httpModule = new HttpModule(this);
        socketModule = new SocketModule(this, port);
        requestModule = new RequestModule();
        balancingModule = new BalancingModule(this);
        System.out.println("[Fire-IO] Attaching to port " + port);
    }

    public FireIoServer on(Event e, Consumer<EventPayload> r) {
        eventHandler.on(e, r);
        return this;
    }

    public Client getClient(UUID id) {
        return clientModule.getClient(id);
    }
    
    public Collection<FireIoConnection> getClients() {return clientModule.getAll();}

    public Client getClientByTag(String key, String value) {
        return clientModule.getAll().stream().filter(client -> client.getInfo().getArguments().containsKey(key)
                && client.getInfo().getArguments().get(key).equals(value)).findFirst().orElse(null);
    }

    public List<Client> getClientsByTag(String key, String value) {
        return clientModule.getAll().stream().filter(client -> client.getInfo().getArguments().containsKey(key)
                && client.getInfo().getArguments().get(key).equals(value)).collect(Collectors.toList());
    }

    public FireIoServer denyConnection(BlockedProtocol protocol) {
        //deny protocol
        socketModule.getBlockedProtocolList().add(protocol);
        return this;
    }

    public FireIoServer setPassword(String password) {
        httpModule.setPassword(password);
        return this;
    }

    public FireIoServer broadcast(String channel, Packet packet) {
        clientModule.connectionMap.values().forEach(c -> {
            if (c.getHandler() != null && c.getHandler().authenticated && c.getHandler().isOpen()) {
                c.send(channel, packet);
            }
        });
        return this;
    }

    public FireIoServer setThreadPoolSize(int size) {
        threadPool.setSize(size);
        return this;
    }

    public FireIoServer onRequest(String type, RequestExecutor executor) {
        requestModule.register(type, executor);
        return this;
    }

    public FireIoServer broadcast(String channel, String message) {
        clientModule.connectionMap.values().forEach(c -> {
            if (c.getHandler() != null && c.getHandler().authenticated && c.getHandler().isOpen()) {
                c.send(channel, message);
            }
        });
        return this;
    }

    public FireIoServer on(String e, Consumer<EventPayload> r) {
        eventHandler.on(e, r);
        return this;
    }

    public FireIoServer linkLoadBalancer(BalancerConfiguration configuration) {
        balancingModule.register(configuration, this);
        return this;
    }

    public FireIoServer registerEndpoint(String path, HttpInteraction exchange) {
        getHttpModule().getHttpRequestProcessor().registerHandler(path, exchange);
        return this;
    }

    public FireIoServer setRateLimiter(int requests, int timeout) {
        httpModule.setRateLimiter(timeout, requests);
        socketModule.getAsyncNetworkService().getSelectorHandler().setRateLimiter(timeout, requests);
        return this;
    }

    @Override
    public ThreadPool getPool() {
        return threadPool;
    }
}
