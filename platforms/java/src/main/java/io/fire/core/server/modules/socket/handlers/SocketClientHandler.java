package io.fire.core.server.modules.socket.handlers;

import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.interfaces.EventPayload;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.io.enums.ConnectionType;
import io.fire.core.common.io.IoManager;
import io.fire.core.common.io.enums.WebSocketStatus;
import io.fire.core.common.io.objects.WebSocketTransaction;
import io.fire.core.common.packets.*;
import io.fire.core.common.interfaces.SocketEvents;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.objects.ClientInfo;

import lombok.Getter;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
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
    @Getter private ConnectionType connectionType = ConnectionType.NONE;

    SocketClientHandler(FireIoServer server, Socket socket, SocketChannel channel) {
        //constructor
        this.server = server;
        this.socket = socket;
        this.ioManager = new IoManager(channel);

        this.ioManager.setPacketHandler(this::onPacket);
        this.ioManager.setWebSocketHandler(input -> {
            try {
                onWebSocketPacket(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
        if (connectionType == ConnectionType.WEBSOCKET) {
            if (p instanceof ChannelMessagePacket) {
                this.ioManager.sendWebSocket("channelmessage:"+((ChannelMessagePacket) p).getChannel()+":"+((ChannelMessagePacket) p).getText());
                return;
            }
            throw new IOException("Fire-IO websocket does not support packet type: " + p.getClass().getName());
        } else {
            this.ioManager.send(p);
        }
    }

    @Override
    public void onPacket(Packet packet) {
        this.connectionType = ConnectionType.FIREIO;
        if (packet instanceof AuthPacket) {
            UUID parsed = UUID.fromString(((AuthPacket) packet).getUuid());
            if (server.getClientModule().getClient(parsed) == null) {
                System.out.println("is null");
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
    public void onWebSocketPacket(WebSocketTransaction webSocketTransaction) throws Exception {
        this.connectionType = ConnectionType.WEBSOCKET;
        //check status, is it allowed? if not, then first finish handshake
        if (webSocketTransaction.getStatus() == WebSocketStatus.IDLE_NEW) {
            //start handshake
            //check if it contains data
            if (!webSocketTransaction.getData().contains("Sec-WebSocket-Key: ")) return;
            //split everything before the value
            String key = webSocketTransaction.getData().split("Sec-WebSocket-Key: ")[1].split("\r\n")[0];
            String token = webSocketTransaction.getData().split("GET /")[1].split(" ")[0];

            //validate auth token
            try {
                UUID parsed = UUID.fromString(token);
                if (parsed == null) {
                    close();
                    return;
                }
                connectionId = parsed;
            } catch (Exception e) {
                e.printStackTrace();
                close();
                return;
            }

            StringBuilder computeInput = new StringBuilder();
            computeInput.append(key);
            computeInput.append("258EAFA5-E914-47DA-95CA-C5AB0DC85B11");

            //compute sha-1 and then convert to base
            String acceptKey = DatatypeConverter.printBase64Binary(MessageDigest.getInstance("SHA-1").digest((computeInput.toString()).getBytes("UTF-8")));

            byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: " + acceptKey
                    + "\r\n\r\n")
                    .getBytes("UTF-8");

            if (server.getClientModule().getClient(connectionId) == null) {
                close();
                return;
            }

            ioManager.setWebSocketStatus(WebSocketStatus.CONNECED);
            ioManager.write(ByteBuffer.wrap(response));
            open = true;
            authenticated = true;
            server.getClientModule().getClient(connectionId).setInfo(new ClientInfo());
            server.getClientModule().getClient(connectionId).setHandler(this);
            server.getEventHandler().fireEvent(Event.CONNECT, server.getClientModule().getClient(connectionId));
            return;
        }

        //handle event
        if (webSocketTransaction.getData().startsWith("channelmessage:")) {
            //it is a simple channel string
            String data = webSocketTransaction.getData();
            data = data.replaceFirst("channelmessage:", "");
            String channel = data.split(":")[0];
            data = data.replaceFirst(channel + ":", "");
            consumer.accept(new ChannelMessagePacket(channel, data));
        }
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
