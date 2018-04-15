package io.fire.core.client;

import io.fire.core.client.modules.rest.RestModule;
import io.fire.core.client.modules.socket.SocketModule;
import io.fire.core.common.events.EventHandler;
import io.fire.core.common.events.enums.Event;
import io.fire.core.common.events.interfaces.Listener;
import io.fire.core.common.interfaces.ClientMeta;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.packets.ChannelMessagePacket;
import io.fire.core.common.packets.ChannelPacketPacket;
import io.fire.core.common.packets.ReceivedText;
import lombok.Getter;

import java.io.IOException;
import java.util.*;

public class FireIoClient {

    //modules
    @Getter private SocketModule socketModule;
    @Getter private RestModule restModule;
    @Getter private EventHandler eventHandler = new EventHandler();

    private String host;
    private int port;
    private UUID id;
    private int connectAttampt = 0;
    private Timer scheduler = new Timer();
    private Map<String, String> connectionArguments = new HashMap<>();
    private Map<String, ClientMeta> argumentsMeta;

    public FireIoClient(String host, int port) {
        this.port = port;
        this.host = host;
        restModule = new RestModule(this, host, (port +1));

        eventHandler.on(Event.CONNECT, a-> {
            connectAttampt = 0;
        });
    }

    public FireIoClient setPassword(String password) {
        restModule.setPassword(password);
        return this;
    }

    public FireIoClient setAutoReConnect(int timeout) {
        eventHandler.on(Event.CLOSED_UNEXPECTEDLY, a-> {
            String message = ((ReceivedText) a).getString();
            getEventHandler().fireEvent(Event.DISCONNECT, null);
            System.err.println("[FireIo] Connection closed unexpectedly! attempting re-connect in " + timeout + "MS.");
            System.err.println(" - Error: " + message);
            connectAttampt++;
            System.err.println(" - Attempt: " + connectAttampt);
            scheduler.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            connect();
                        }
                    }, timeout);
        });

        return this;
    }

    public FireIoClient setParameter(String s, String b) {
        connectionArguments.put(s, b);
        return this;
    }

    public FireIoClient setMeta(String s, ClientMeta meta) {
        argumentsMeta.put(s, meta);
        return this;
    }

    public FireIoClient connect() {
        System.out.println("[FireIO] starting client & requesting token");

        String a = restModule.getToken();

        if (a == null) {
            getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, new ReceivedText("Failed to get api key" ,null));
            return this;
        }

        if (a.equals("ratelimit")) {
            getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, new ReceivedText("Connection blocked by ratelimiter" ,null));
            return this;
        }

        if (a.equals("fail-auth")) {
            getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, new ReceivedText("Failed to authenticate, is your password correct?" ,null));
            return this;
        }

        UUID b = UUID.fromString(a);

        if (b == null) {
            getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, new ReceivedText("Failed to parse api key" ,null));
            return this;
        }

        id = b;

        socketModule = new SocketModule(this, host, port, id, connectionArguments, argumentsMeta);
        return this;
    }

    public FireIoClient send(String channel, Packet packet) {
        try {
            socketModule.getConnection().emit(new ChannelPacketPacket(null, channel, packet));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public FireIoClient send(String channel, String message) {
        try {
            socketModule.getConnection().emit(new ChannelMessagePacket(channel, message));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public FireIoClient close() {
        socketModule.getConnection().close();
        return this;
    }

    public FireIoClient stop() {
        return close();
    }

    public FireIoClient on(Event e, Listener r) {
        eventHandler.on(e, r);
        return this;
    }

    public FireIoClient on(String e, Listener r) {
        eventHandler.on(e, r);
        return this;
    }

}
