package io.fire.core.server.modules.client.objects;

import io.fire.core.common.interfaces.ClientMeta;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.packets.ChannelMessagePacket;
import io.fire.core.common.packets.ChannelPacketPacket;
import io.fire.core.common.packets.ReceivedText;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.superclasses.Client;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.server.modules.socket.handlers.SocketClientHandler;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.UUID;

public class FireIoConnection extends Client {

    @Getter private SocketClientHandler handler;
    private FireIoServer server;
    @Setter private ClientInfo info;

    public FireIoConnection(FireIoServer server) {
        setId(UUID.randomUUID());
        this.server = server;
    }

    @Override
    public void send(String channel, Packet packet) {
        if (handler != null && handler.authenticated && handler.isOpen()) {
            try {
                handler.emit(new ChannelPacketPacket(null, channel, packet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ClientMeta getMeta(String key) {
        return info.getArgumentsMeta().get(key);
    }

    @Override
    public String getTag(String key) {
        return info.getArguments().get(key);
    }

    @Override
    public ClientInfo getInfo() {
        return info;
    }

    @Override
    public void send(String channel, String message) {
        if (handler.authenticated && handler.isOpen()) {
            try {
                handler.emit(new ChannelMessagePacket(channel, message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        if (handler != null) {
            if (handler.isOpen()) {
                handler.close();
            }
        }
        server.getEventHandler().fireEvent(Event.DISCONNECT, this);
        server.getClientModule().removeClient(getId());
    }

    public void setHandler(SocketClientHandler handler) {
        this.handler = handler;
        handler.onMessage(packet -> {
            if (packet instanceof ChannelMessagePacket) {
                ChannelMessagePacket messagePacket = (ChannelMessagePacket) packet;
                server.getEventHandler().fireEvent(
                        messagePacket.getChannel(),
                        new ReceivedText(messagePacket.getText(),
                                this));
                return;
            }

            if (packet instanceof ChannelPacketPacket) {
                ChannelPacketPacket packetPacket = (ChannelPacketPacket) packet;
                packetPacket.setSender(this);
                server.getEventHandler().fireEvent(packetPacket.getChannel(), packetPacket);
            }
        });
    }



}
