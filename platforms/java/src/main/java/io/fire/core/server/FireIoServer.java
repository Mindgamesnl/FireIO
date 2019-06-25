package io.fire.core.server;

import io.fire.core.common.events.EventHandler;
import io.fire.core.common.events.enums.Event;
import io.fire.core.common.events.enums.EventPriority;
import io.fire.core.common.events.executors.EventExecutor;
import io.fire.core.common.io.socket.interfaces.GenericClient;
import io.fire.core.common.io.socket.interfaces.Packet;
import io.fire.core.server.modules.http.HttpProvider;
import io.fire.core.server.modules.socket.SocketServer;
import lombok.Getter;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FireIoServer {

    //modules
    @Getter private int port;
    @Getter private HttpProvider httpProvider;
    @Getter private SocketServer socketServer;
    @Getter private EventHandler eventHandler = new EventHandler();

    public FireIoServer(int port) throws IOException {
        this.port = port;
        this.httpProvider = new HttpProvider(this);
        this.socketServer = new SocketServer(this, port);
        System.out.println("[Fire-IO] Attaching to port " + port);
    }

    public FireIoServer setPassword(String password) {
        this.socketServer.setPassword(password);
        return this;
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
    public FireIoServer on(Event e, Consumer<GenericClient> r) {
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
    public FireIoServer on(Event e, BiConsumer<GenericClient, String> r) {
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
    public FireIoServer on(String string, BiConsumer<GenericClient, String> r) {
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
}
