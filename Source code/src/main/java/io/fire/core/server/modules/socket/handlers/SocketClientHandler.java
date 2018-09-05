package io.fire.core.server.modules.socket.handlers;

import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.interfaces.EventPayload;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.io.IoManager;
import io.fire.core.common.io.WebSocketStatus;
import io.fire.core.common.io.WebSocketTransaction;
import io.fire.core.common.packets.*;
import io.fire.core.common.interfaces.SocketEvents;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.objects.ClientInfo;

import lombok.Getter;
import sun.misc.BASE64Encoder;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
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
        this.ioManager.setWebSocketHandler(input -> {
            try {
                onWebsocketPacket(input);
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
    public void onWebsocketPacket(WebSocketTransaction webSocketTransaction) throws Exception {
        //check if the connection is old, if it is, handle the startup section
        if (webSocketTransaction.getStatus() == WebSocketStatus.IDLE_NEW) {
            Map<String, String> details = new HashMap<>();
            webSocketTransaction.getData().forEach(s -> {
                String[] packet = s.split(": ");
                if (packet.length == 2) details.put(packet[0], packet[1]);
            });

            System.out.println("hash: " + details.get("Sec-WebSocket-Key").replaceAll(" ", ""));
            System.out.println("r: " + DatatypeConverter
                    .printBase64Binary(
                            MessageDigest
                                    .getInstance("SHA-1")
                                    .digest((details.get("Sec-WebSocket-Key"))
                                            .getBytes("UTF-8"))));

            byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: "
                    + getKey(details.get("Sec-WebSocket-Key"))
                    + "\r\n\r\n")
                    .getBytes("UTF-8");

            System.out.println("SenD: " + new String(response));

            ByteBuffer buf = ByteBuffer.wrap(response);
            this.ioManager.write(buf);
            this.ioManager.setWebSocketStatus(WebSocketStatus.HANDSHAKE);
        }
    }

    public static String getKey(String strWebSocketKey) throws
            NoSuchAlgorithmException {

        strWebSocketKey += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

        MessageDigest shaMD = MessageDigest.getInstance("SHA-1");
        shaMD.reset();
        shaMD.update(strWebSocketKey.getBytes());
        byte messageDigest[] = shaMD.digest();
        BASE64Encoder b64 = new BASE64Encoder();

        return b64.encode(messageDigest);

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
