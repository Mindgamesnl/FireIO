package io.fire.core.server.modules.socket.handlers;

import io.fire.core.common.events.enums.Event;
import io.fire.core.common.events.interfaces.Listener;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.SerialReader;
import io.fire.core.common.packets.*;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.objects.ClientInfo;
import io.fire.core.server.modules.socket.interfaces.SocketEvents;

import lombok.Getter;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class SocketClientHandler extends SerialReader implements SocketEvents {

    private Socket socket;
    private Listener listener;
    private FireIoServer server;
    public boolean authenticated = false;
    private SocketChannel channel;
    private Queue<Packet> missedPackets = new LinkedList<>();
    private UUID connectionId;
    private boolean exptectedClosing = false;
    @Getter
    private boolean open = true;
    @Getter
    private Date initiated = new Date();

    SocketClientHandler(FireIoServer server, Socket socket, SocketChannel channel) {
        this.server = server;
        this.socket = socket;
        this.channel = channel;
    }

    public void onMessage(Listener listener) {
        this.listener = listener;
    }

    public void close() {
        try {
            emit(new PrepareClosingConnection());
            socket.close();
            open = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void emit(Packet p) throws IOException {
        if (!authenticated) {
            return;
        }
        try {
            String out = toString(p);

            if (server.getSocketModule().getAsyncNetworkService().getSelectorHandler().getByteArrayLength() < out.getBytes().length) {
                server.getSocketModule().getAsyncNetworkService().getSelectorHandler().setUpdatedBuffer(true);
                server.getSocketModule().getAsyncNetworkService().getSelectorHandler().setByteArrayLength(out.getBytes().length);
                server.getSocketModule().getAsyncNetworkService().broadcast(new UpdateByteArraySize(out.getBytes().length));
            }

            ByteBuffer buffer = ByteBuffer.allocate(out.getBytes().length);
            buffer.put(out.getBytes());
            buffer.flip();
            channel.write(buffer);
            buffer.clear();
        } catch (Exception e) {
            missedPackets.add(p);
            throw new IOException("Failed to send! saving to retry once a connection is back active. reason: " + e.getMessage());
        }
    }

    @Override
    public void onPacket(Packet packet) {
        if (!authenticated) {
            if (!(packet instanceof AuthPacket)) {
                close();
                return;
            }
            UUID parsed = UUID.fromString(((AuthPacket) packet).getUuid());
            if (parsed == null) {
                close();
                return;
            } else {
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
                    while (missedPackets.size() != 0) {
                        Packet p = missedPackets.peek();
                        try {
                            emit(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                        missedPackets.remove(p);
                    }
                    return;
                }
            }
        }

        if (packet instanceof UpdateByteArraySize) {
            server.getSocketModule().getAsyncNetworkService().getSelectorHandler().setUpdatedBuffer(true);
            server.getSocketModule().getAsyncNetworkService().getSelectorHandler().setByteArrayLength(((UpdateByteArraySize) packet).getSize());
            server.getSocketModule().getAsyncNetworkService().broadcast(new UpdateByteArraySize(((UpdateByteArraySize) packet).getSize()));
            return;
        }

        if (packet instanceof PrepareClosingConnection) {
            exptectedClosing = true;
            return;
        }

        if (packet instanceof SubmitRequestPacket) {
            SubmitRequestPacket submitRequestPacket = (SubmitRequestPacket) packet;
            server.getRequestModule().trigger(submitRequestPacket.getId(), submitRequestPacket.getPayload(), server.getClient(connectionId), submitRequestPacket.getRequestId());
            return;
        }

        if (listener != null) listener.call(packet);
    }

    @Override
    public void onClose() {
        //fire io's garbage collector will clean it up so this is not a memory leak!
        if (exptectedClosing) {
            server.getEventHandler().fireEvent(Event.DISCONNECT, server.getClientModule().getClient(connectionId));
            authenticated = false;
            connectionId = null;
            open = false;
        } else {
            server.getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, server.getClientModule().getClient(connectionId));
            authenticated = false;
            connectionId = null;
        }
    }

    @Override
    public void onOpen() {
        exptectedClosing = false;
        if (server.getSocketModule().getAsyncNetworkService().getSelectorHandler().isUpdatedBuffer()) {
            try {
                emit(new UpdateByteArraySize(server.getSocketModule().getAsyncNetworkService().getSelectorHandler().getByteArrayLength()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
