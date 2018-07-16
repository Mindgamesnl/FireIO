package io.fire.core.client.modules.socket.handlers;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.socket.reader.IoReader;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.interfaces.EventPayload;
import io.fire.core.common.interfaces.ClientMeta;
import io.fire.core.common.interfaces.ConnectedFireioClient;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.objects.PacketHelper;
import io.fire.core.common.packets.*;
import io.fire.core.server.modules.socket.interfaces.SocketEvents;

import java.io.IOException;
import java.net.InetSocketAddress;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

public class AsyncConnectionHandler implements SocketEvents, EventPayload, ConnectedFireioClient {

    //Main FireIo client!
    //contains all the functional logic, authentication, data (like meta and ID) and handlers
    //this is not the only client object, there is another one for API usage, this is only the engine and connection handler.

    //client date
    private UUID identifier;
    private boolean exptectedClosing = false;
    private Map<String, String> arguments;
    private Map<String, ClientMeta> argumentsMeta;

    //api, connection handler/receiver, channel and reader thread
    private FireIoClient client;
    private IoReader ioReader;
    private SocketChannel socketChannel;
    private Thread reader;
    private PacketHelper packetHelper;

    //host information for connection
    private String host;
    private int port;
    private Boolean isSetup = false;
    private Boolean isDead = false;

    //buffers!
    //when the client failed to send a packet, it will retry at a moment to prevent packet loss.
    private Queue<Packet> missedPackets = new LinkedList<>();
    private List<Packet> bufferedPackets = new ArrayList<>();

    //constructor, apply data and try to connect. if the connection fails, then handle it appropriately
    public AsyncConnectionHandler(FireIoClient client, String host, int port, UUID id, Map<String, String> arguments, Map<String, ClientMeta> argumentsMeta) {
        try {
            this.identifier = id;
            this.client = client;
            this.host = host;
            this.port = port;
            this.arguments = arguments;
            this.argumentsMeta = argumentsMeta;
            this.packetHelper = new PacketHelper(client.getEventHandler());
            connect();
        } catch (IOException e) {
            //trigger fatal error event for api and internal ussage
            client.getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, new ReceivedText("Connection timed out! (could not initiate handshake)" ,null));
        }
    }

    private void connect() throws IOException {
        //open a socket channel, create a socket reader with a default buffer, and then try to authenticate over the newly created socket.
        //the default buffer is common in everything of Fire-Io, when bigger data is getting send it will change in the whole network to what ever is needed.
        //The default is 5KB
        socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        if (reader != null && reader.isAlive()) reader.stop();
        //create reader
        ioReader = new IoReader(socketChannel, 5120, this, client);
        reader = new Thread(ioReader);
        //start reader
        reader.start();
        //start handshake process
        emit(new AuthPacket(identifier.toString(), System.getProperty("os.name"), arguments, argumentsMeta));
    }

    public void emit(Packet p) throws IOException {
        try {
            //serialize packet
            byte[] out = packetHelper.toString(p);
            //check if the packet fits in the buffer, if it does not, then request to increase the size of the buffer and when its chaned and confirmed, then retry to send it.
            //the list "bufferedPackets" will be checked for packets every time the buffer size changes
            //the changes HAVE TO BE DONE BY THE SERVER! the client is ALWAYS slave!
            if (ioReader.getBufferSize() < out.length) {
                //update local
                ioReader.setBufferSize(out.length);
                //request server to accept it as new global and sync it to the other clients
                emit(new UpdateByteArraySize(out.length));
                //add to cache
                bufferedPackets.add(p);
                //cancel sending packet, wait until the buffer size got updated
                return;
            }

            //create and allocate and prepare bytebyffer
            ByteBuffer buffer = ByteBuffer.allocate(out.length);
            buffer.put(out);
            buffer.flip();

            //send raw bytes through the channel
            socketChannel.write(buffer);
            //clear the buffer
            buffer.clear();
        } catch (Exception e) {
            //failed to send! (io exception, socket may be closed or busy)
            //add the packet to a list so it can retry to emit once a new connection is open
            missedPackets.add(p);
            e.printStackTrace();
            //throw exception
            throw new IOException("Failed to send! saving to retry once a connection is back active.");
        }
    }

    public void close() {
        try {
            //let the server know we intend to close the connection, this prevents falsely labeled errors.
            if (isSetup) {
                emit(new PrepareClosingConnection());
            }
            isDead = true;
            //close channel
            socketChannel.close();
            //stop reader thread
            reader.stop();
        } catch (IOException e) {
            //catch for the emit function, dont really care if it goes or not since we are closing it anyhow
            e.printStackTrace();
        }
    }

    @Override
    public void onPacket(Packet packet) {
        if (isDead) return;
        //function from the SocketEvents interface
        //used to handle packets from the io reader
        //this function handles internal fireio packets and supply's custom packets to their api events

        //server accepted client! they are now connected and authenticated
        if (packet instanceof FinishHandshake) {
            //trigger connected event
            isSetup = true;
            client.getEventHandler().fireEvent(Event.CONNECT, null);
            //check for previously failed packets and retry them in order that they where attempted to send
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

        //the server intends to close our connection
        if (packet instanceof PrepareClosingConnection) {
            //flag intended closure of connection, handle it as cleanly closed and dont try to re connect when the ioreader spits a timed out error
            exptectedClosing = true;
            return;
        }

        //finish a pending request from the request api
        if (packet instanceof CompleteRequestPacket) {
            //the server completed a pending request and has send its response body (request awnser) and id that goes with it
            //cast correct packet
            CompleteRequestPacket cpr = (CompleteRequestPacket) packet;
            //finish handling request in the request module
            client.getClientRequestModule().handleRequestResponse(cpr.getRequestId(), cpr.getResult());
            return;
        }

        //server forces us to accept a new byte array buffer size as standard
        if (packet instanceof UpdateByteArraySize) {
            //another client wants to send bigger packets and thus we have to increase the size of our byte array
            //all incoming and outgoing packets will now use this buffer size

            //update buffer size based on what the server told in the packet (client is slave)
            ioReader.setBufferSize(((UpdateByteArraySize) packet).getSize());

            //check cache for packets we can now send we previously couldn't, emit them if fount.
            for (Packet bp : bufferedPackets) {
                byte[] out = packetHelper.toString(bp);
                //check if we can really send them now
                if (ioReader.getBufferSize() >= out.length) {
                    try {
                        //emit
                        emit(bp);
                    } catch (IOException e) {
                        //oi fuck no error
                        e.printStackTrace();
                    }
                    return;
                }
            }
            //clear buffer
            bufferedPackets.clear();
            return;
        }

        //cancel the request, server does not know how to handle it
        if (packet instanceof CancelRequestPacket) {
            //cast to proper packet
            CancelRequestPacket cancellation = (CancelRequestPacket) packet;
            client.getClientRequestModule().cancel(cancellation.getRequest());
        }

        //server has triggered a custom event with a custom packet as payload
        if (packet instanceof ChannelPacketPacket) {
            //cast the correct internal packet
            ChannelPacketPacket packetPacket = (ChannelPacketPacket) packet;
            //trigger event handler with channel and custom packet
            client.getEventHandler().fireEvent(packetPacket.getChannel(), packetPacket);
            return;
        }

        //server has triggered a custom event with a string as payload
        if (packet instanceof ChannelMessagePacket) {
            //cast the correct internal packet
            ChannelMessagePacket message = (ChannelMessagePacket) packet;
            //trigger event handler with channel and newly created text based payload
            client.getEventHandler().fireEvent(message.getChannel(), new ReceivedText(message.getText(), null));
            return;
        }
    }

    @Override
    public void onClose() {
        //the ioreader detected that the channel closed or dropped connection
        //stop the reader, its no longer of any use since it died
        reader.stop();
        //check if it was expected to close
        if (exptectedClosing) {
            //it was expected to close, either we or the server has requested it
            client.getEventHandler().fireEvent(Event.DISCONNECT, null);
        } else {
            //MURDER!
            //it timed out or died a un expected death!
            //trigger event and possibly trigger auto reconnect (if set)
            client.getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, this);
        }
    }

    @Override
    public void onOpen() {
        //a EMPTY & DEFAULT connection opened, reset default values
        exptectedClosing = false;
        //(dent send anything yet since we are not authenticated yet and thus the server will ignore it)
    }

    @Override
    public void send(String channel, String message) {
        //function from the ConnectedFireioClient interface for use in the API
        //the user wants to send a piece of text via a channel
        try {
            //create a internal packet containing the channel and the message and then send it
            emit(new ChannelMessagePacket(channel, message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(String channel, Packet packet) {
        //function from the ConnectedFireioClient interface for use in the API
        //the user wants to send a custom packet via a channel
        try {
            //create a internal packet with channel and the custom packet
            //the sender value is only used on the server side in the event handler, we need to leave it empty here since the server will fill it in
            emit(new ChannelPacketPacket(null, channel, packet));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UUID getId() {
        //function from the ConnectedFireioClient interface for use in the API
        //return client uuid
        return identifier;
    }
}
