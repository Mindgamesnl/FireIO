package io.fire.core.client.modules.socket.handlers;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.socket.reader.IoReader;
import io.fire.core.common.events.enums.Event;
import io.fire.core.common.events.interfaces.EventPayload;
import io.fire.core.common.interfaces.ClientMeta;
import io.fire.core.common.interfaces.ConnectedFireioClient;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.SerialReader;
import io.fire.core.common.packets.*;
import io.fire.core.server.modules.socket.interfaces.SocketEvents;

import java.io.IOException;
import java.net.InetSocketAddress;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;


public class AsyncConnectionHandler extends SerialReader implements SocketEvents, EventPayload, ConnectedFireioClient {

    private UUID identifier;
    private FireIoClient client;
    private boolean authenticated = false;
    private boolean running = true;
    private Queue<Packet> missedPackets = new LinkedList<>();
    private boolean exptectedClosing = false;
    private IoReader ioReader;
    private Map<String, String> arguments;
    private Map<String, ClientMeta> argumentsMeta;

    private String host;
    private int port;

    //socket stuffs
    private SocketChannel socketChannel;
    private Thread reader;

    public AsyncConnectionHandler(FireIoClient client, String host, int port, UUID id, Map<String, String> arguments, Map<String, ClientMeta> argumentsMeta) {
        try {
            this.identifier = id;
            this.client = client;
            this.host = host;
            this.port = port;
            this.arguments = arguments;
            this.argumentsMeta = argumentsMeta;
            connect();

        } catch (IOException e) {
            client.getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, new ReceivedText("Connection timed out! (could not initiate handshake)" ,null));
            return;
        }

    }

    private void connect() throws IOException {
        socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        if (reader != null && reader.isAlive()) reader.stop();
        ioReader = new IoReader(socketChannel, 1024, this, client);
        reader = new Thread(ioReader);
        reader.start();
        emit(new AuthPacket(identifier.toString(), System.getProperty("os.name"), arguments, argumentsMeta));

    }

    public void emit(Packet p) throws IOException {
        try {
            String out = toString(p);
            if (ioReader.getBufferSize() < out.getBytes().length) {
                ioReader.setBufferSize(out.getBytes().length);
                emit(new UpdateByteArraySize(out.getBytes().length));
            }


            ByteBuffer buffer = ByteBuffer.allocate(out.getBytes().length);
            buffer.put(out.getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        } catch (Exception e) {
            missedPackets.add(p);
            e.printStackTrace();
            throw new IOException("Failed to send! saving to retry once a connection is back active.");
        }
    }



    public void close() {
        try {
            emit(new PrepareClosingConnection());
            running = false;
            socketChannel.close();
            reader.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPacket(Packet packet) {
        if (packet instanceof FinishHandshake) {
            authenticated = true;
            client.getEventHandler().fireEvent(Event.CONNECT, null);
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
        }

        if (packet instanceof PrepareClosingConnection) {
            exptectedClosing = true;
            return;
        }

        if (packet instanceof UpdateByteArraySize) {
            ioReader.setBufferSize(((UpdateByteArraySize) packet).getSize());
            return;
        }

        if (packet instanceof ChannelPacketPacket) {
            ChannelPacketPacket packetPacket = (ChannelPacketPacket) packet;
            client.getEventHandler().fireEvent(packetPacket.getChannel(), packetPacket);
            return;
        }

        if (packet instanceof ChannelMessagePacket) {
            ChannelMessagePacket message = (ChannelMessagePacket) packet;
            client.getEventHandler().fireEvent(message.getChannel(), new ReceivedText(message.getText(), null));
            return;
        }
    }

    @Override
    public void onClose() {
        reader.stop();
        if (exptectedClosing) {
            client.getEventHandler().fireEvent(Event.DISCONNECT, null);
        } else {
            client.getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, this);
        }
    }

    @Override
    public void onOpen() {
        exptectedClosing = false;
        authenticated = false;
    }

    @Override
    public void send(String channel, String message) {
        try {
            emit(new ChannelMessagePacket(channel, message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(String channel, Packet packet) {
        try {
            emit(new ChannelPacketPacket(null, channel, packet));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UUID getId() {
        return identifier;
    }
}
