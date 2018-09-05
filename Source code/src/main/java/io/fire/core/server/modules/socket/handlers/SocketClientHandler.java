package io.fire.core.server.modules.socket.handlers;

import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.interfaces.EventPayload;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.io.IoManager;
import io.fire.core.common.packets.*;
import io.fire.core.common.interfaces.SocketEvents;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.objects.ClientInfo;

import lombok.Getter;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

public class SocketClientHandler implements SocketEvents {

    //java socket channel and socket
    private Socket socket;

    //packet listener/handler
    private Consumer<EventPayload> consumer;
    private IoManager ioManager;

    //main instance
    private FireIoServer server;

    //meta and connection info
    public boolean authenticated = false;
    private UUID connectionId;
    private boolean expectedClosing = false;
    @Getter private boolean open = true;
    @Getter private Instant initiated = Instant.now();

    SocketClientHandler(FireIoServer server, Socket socket, SocketChannel channel) {
        //constructor
        this.server = server;
        this.socket = socket;
        this.ioManager = new IoManager(channel);

        this.ioManager.setPacketHandler(input -> onPacket(input));
    }

    public void onMessage(Consumer<EventPayload> listener) {
        //set message handler
        this.consumer = listener;
    }

    public void close() {
        //close connection
        try {
            //let client know that we intend to close the connection
            emit(new PrepareClosingConnection());
            socket.close();
            open = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void emit(Packet p) throws IOException {
        if (!authenticated) return;
        this.ioManager.send(p);
    }

    @Override
    public void onPacket(Packet packet) {
        if (packet instanceof AuthPacket) {
            UUID parsed = UUID.fromString(((AuthPacket) packet).getUuid());
            if (server.getClientModule().getClient(parsed) == null) {
                close();
                return;
            } else {
                connectionId = parsed;
                authenticated = true;
                ClientInfo clientInfo = new ClientInfo();
                clientInfo.setArguments(((AuthPacket) packet).getArguments());
                clientInfo.setArgumentsMeta(((AuthPacket) packet).getArgumentsMeta());
                clientInfo.setHostname(socket.getInetAddress().getHostName());
                clientInfo.setPlatform(((AuthPacket) packet).getPlatform());
                server.getClientModule().getClient(connectionId).setInfo(clientInfo);
                server.getClientModule().getClient(connectionId).setHandler(this);
                server.getEventHandler().fireEvent(Event.CONNECT, server.getClientModule().getClient(connectionId));
                try {
                    emit(new FinishHandshake());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.ioManager.flushWaiting();
                return;
            }
        }

        if (!authenticated) {
            close();
            return;
        }

        if (packet instanceof PrepareClosingConnection) {
            expectedClosing = true;
            return;
        }

        if (packet instanceof SubmitRequestPacket) {
            SubmitRequestPacket submitRequestPacket = (SubmitRequestPacket) packet;
            server.getRequestModule().trigger(submitRequestPacket.getId(), submitRequestPacket.getPayload(), server.getClient(connectionId), submitRequestPacket.getRequestId());
            return;
        }

        if (consumer != null) consumer.accept(packet);
    }

    @Override
    public void onClose() {
        //fire io's garbage collector will clean it up so this is not a memory leak!
        if (expectedClosing) {
            authenticated = false;
            open = false;
            server.getEventHandler().fireEvent(Event.DISCONNECT, server.getClientModule().getClient(connectionId));
            connectionId = null;
        } else {
            authenticated = false;
            if (server.getClientModule().getClient(connectionId) != null) server.getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, server.getClientModule().getClient(connectionId));
            connectionId = null;
        }
    }

    @Override
    public void onOpen() {
        expectedClosing = false;
    }

    @Override
    public IoManager getIoManager() {
        return ioManager;
    }
}
