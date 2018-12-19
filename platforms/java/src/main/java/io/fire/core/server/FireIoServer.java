package io.fire.core.server;

import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.EventHandler;
import io.fire.core.common.eventmanager.enums.EventPriority;
import io.fire.core.common.eventmanager.executors.EventExecutor;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.PoolHolder;
import io.fire.core.common.io.api.request.HttpInteraction;
import io.fire.core.common.io.enums.InstanceSide;
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
import java.util.function.BiConsumer;
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


    /**
     * Main FireIoServer function
     *
     * This sets up and starts a server at any given port that is not already in use.
     * the server starts immediately after setting up
     *
     * @param port
     * @throws IOException
     */
    public FireIoServer(int port) throws IOException {
        this.port = port;
        eventHandler = new EventHandler(InstanceSide.SERVER);
        clientModule = new ClientModule(this);
        httpModule = new HttpModule(this);
        socketModule = new SocketModule(this, port);
        requestModule = new RequestModule();
        balancingModule = new BalancingModule();
        System.out.println("[Fire-IO] Attaching to port " + port);
    }


    /**
     * Register an event handler for a specific Event
     * Mostly used for Fire-IO native API
     *
     * Deprecated since the Introduction of the new event api.
     *
     * @param e
     * @param r
     * @return
     */
    @Deprecated
    public FireIoServer on(Event e, Consumer<Client> r) {
        eventHandler.registerEvent(e).onExecute((client, string) -> r.accept(client));
        return this;
    }


    /**
     * Register an event handler for a specific Event
     * Mostly used for Fire-IO native API (with string payload)
     *
     * Deprecated since the Introduction of the new event api.
     *
     * @param e
     * @param r
     * @return
     */
    @Deprecated
    public FireIoServer on(Event e, BiConsumer<Client, String> r) {
        eventHandler.registerEvent(e).onExecute(r);
        return this;
    }


    /**
     * Register an string handler for a specific Channel
     * Mostly used for Fire-IO native API (with string payload)
     *
     * Deprecated since the Introduction of the new event api.
     *
     * @param r
     * @return
     */
    @Deprecated
    public FireIoServer on(String string, BiConsumer<Client, String> r) {
        eventHandler.registerTextChannel(string, EventPriority.NORMAL).onExecute((client, message) -> r.accept(client, message));
        return this;
    }


    /**
     * New event API
     * This registers a handler for a packet class on a channel with a priority
     *
     * @param packet
     * @param channel
     * @param priority
     * @param <E>
     * @return
     */
    public <E extends Packet> EventExecutor<E> onPacket(Class<E> packet, String channel, EventPriority priority) {
        return eventHandler.registerEvent(packet, channel, priority);
    }


    /**
     * New event API
     * This registers a handler for a packet class on a channel with a normal priority
     *
     * @param packet
     * @param channel
     * @param <E>
     * @return
     */
    public <E extends Packet> EventExecutor<E> onPacket(Class<E> packet, String channel) {
        return eventHandler.registerEvent(packet, channel , EventPriority.NORMAL);
    }


    /**
     * Get a CONNECTED Fire-IO client by UUID
     *
     * @param id
     * @return
     */
    public Client getClient(UUID id) {
        return clientModule.getClient(id);
    }


    /**
     * Returns a collection of all READY, CONNECTED and HEALTHY clients
     *
     * @return
     */
    public Collection<FireIoConnection> getClients() {return clientModule.getAll();}


    /**
     * Sorts clients and filters for tag, used for getting a specific one when you've got a bunch connected
     * @param key
     * @param value
     * @return
     */
    public Client getClientByTag(String key, String value) {
        return clientModule.getAll().stream()
                .filter(client -> client.getInfo().getArguments().containsKey(key)
                && client.getInfo().getArguments().get(key).equals(value))
                .findFirst()
                .orElse(null);
    }


    /**
     * Get all clients that have a tag with value set
     * used for filtering for types
     *
     * @param key
     * @param value
     * @return
     */
    public List<Client> getClientsByTag(String key, String value) {
        return clientModule.getAll().stream().filter(client -> client.getInfo().getArguments().containsKey(key)
                && client.getInfo().getArguments().get(key).equals(value)).collect(Collectors.toList());
    }


    /**
     * Deny connection is used to BLACKLIST a connection type.
     * For instance, to setup a web-server to deny ALL socket connections.
     *
     * @param protocol
     * @return
     */
    public FireIoServer denyConnection(BlockedProtocol protocol) {
        //deny protocol
        socketModule.getBlockedProtocolList().add(protocol);
        return this;
    }


    /**
     * Set a password for the network.
     * Not set (open) by default.
     *
     * Can also be used to UPDATE the password in a already connected instance.
     * All current clients will remain connected, but new clients will have to use the new password.
     *
     * @param password
     * @return
     */
    public FireIoServer setPassword(String password) {
        httpModule.setPassword(password);
        return this;
    }


    /**
     * Broadcast a packet over a channel.
     * Same effect as sending it to a client, but this targets all clients thay are ready to accept packets.
     *
     * @param channel
     * @param packet
     * @return
     */
    public FireIoServer broadcast(String channel, Packet packet) {
        clientModule.connectionMap.values().forEach(c -> {
            if (c.getHandler() != null && c.getHandler().authenticated && c.getHandler().isOpen()) {
                c.send(channel, packet);
            }
        });
        return this;
    }


    /**
     * set the threadpoolsize, default is 1
     *
     * threadpools are used to execute tasks like requests and events everything is async.
     * you may find yourself increasing the threadpool if you do stuff like database query's inside of your networking
     *
     * @param size
     * @return
     */
    public FireIoServer setThreadPoolSize(int size) {
        threadPool.setSize(size);
        return this;
    }


    /**
     * Register a request handler
     * the type is the name of a request, that is used every time you want to trigger your executor.
     *
     * the executor is where you handle your request and finish it
     *
     * @param type
     * @param executor
     * @return
     */
    public FireIoServer onRequest(String type, RequestExecutor executor) {
        requestModule.register(type, executor);
        return this;
    }


    /**
     * Broadcast a message (String) over a channel.
     * Same effect as sending it to a client, but this targets all clients thay are ready to accept packets.
     *
     * @param channel
     * @param message
     * @return
     */
    public FireIoServer broadcast(String channel, String message) {
        clientModule.connectionMap.values().forEach(c -> {
            if (c.getHandler() != null && c.getHandler().authenticated && c.getHandler().isOpen()) {
                c.send(channel, message);
            }
        });
        return this;
    }


    /**
     * Connect and setup a loadbalancer, the loadbalancer configuration is your loadbalancer password, host and port
     * you can setup multiple loadbalancers at the same time
     *
     * @param configuration
     * @return
     */
    public FireIoServer linkLoadBalancer(BalancerConfiguration configuration) {
        balancingModule.register(configuration, this);
        return this;
    }


    /**
     * register HTTP api endpoints
     *
     * String is your path (like /api/v2/get/?name) with ? to assign variables.
     * the exhange is where you handle it
     *
     * @param path
     * @param exchange
     * @return
     */
    public FireIoServer registerEndpoint(String path, HttpInteraction exchange) {
        getHttpModule().getHttpRequestProcessor().registerHandler(path, exchange);
        return this;
    }


    /**
     * set or change the rate limiter settings
     * this overwrites the current or default settings
     *
     * @param requests
     * @param timeout
     */
    public FireIoServer setRateLimiter(int requests, int timeout) {
        httpModule.setRateLimiter(timeout, requests);
        socketModule.setRateLimiter(timeout, requests);
        return this;
    }


    /**
     * Change the amount of socket handlers, only for those who know
     *
     * @param target
     * @return
     */
    public FireIoServer setHandlerLimit(int target) {
        try {
            socketModule.setHandlerAmount(target);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }


    /**
     * get thread pool, for internal usage
     *
     * @return
     */
    @Override
    public ThreadPool getPool() {
        return threadPool;
    }
}
