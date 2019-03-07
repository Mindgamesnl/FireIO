package io.fire.core.client.modules.socket.handlers;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.socket.reader.IoReader;
import io.fire.core.client.modules.socket.task.PingCheckTask;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.interfaces.ClientMeta;
import io.fire.core.common.interfaces.ConnectedFireioClient;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.io.IoManager;
import io.fire.core.common.io.enums.InstanceSide;
import io.fire.core.common.io.objects.WebSocketTransaction;
import io.fire.core.common.packets.*;
import io.fire.core.common.interfaces.SocketEvents;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.InetSocketAddress;

import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.*;

public class AsyncConnectionHandler implements SocketEvents, ConnectedFireioClient  {

    //Main FireIo client!
    //contains all the functional logic, authentication, data (like meta and ID) and handlers
    //this is not the only client object, there is another one for API usage, this is only the engine and connection handler.

    //client date
    private UUID identifier;
    private boolean exptectedClosing = false;
    private Map<String, String> arguments;
    private Map<String, ClientMeta> argumentsMeta;

    //api, connection handler/receiver, channel and reader thread
    @Getter private FireIoClient client;
    @Setter @Getter private Instant lastPing = Instant.now();
    private SocketChannel socketChannel;
    private Thread reader;
    private IoManager ioManager;
    private Timer timer;

    //host information for connection
    @Getter private Boolean isSetup = false;
    @Getter private Boolean isDead = false;
    private String host;
    private int port;

    //buffers!
    //when the client failed to send a packet, it will retry at a moment to prevent packet loss.
    private Queue<Packet> missedPackets = new LinkedList<>();


    /**
     * Setup the AsyncConnectionHandler
     * This handler handles all IO and distrubutes it across the API
     *
     * @param client
     * @param host
     * @param port
     * @param id
     * @param arguments
     * @param argumentsMeta
     */
    public AsyncConnectionHandler(FireIoClient client, String host, int port, UUID id, Map<String, String> arguments, Map<String, ClientMeta> argumentsMeta) {
        try {
            this.timer = new Timer();
            this.timer.schedule(new PingCheckTask(this), 0, 1000);
            this.identifier = id;
            this.client = client;
            this.host = host;
            this.port = port;
            this.arguments = arguments;
            this.argumentsMeta = argumentsMeta;
            connect();
        } catch (IOException e) {
            //trigger fatal error event for api and internal ussage
            client.getEventHandler().triggerEvent(Event.TIMED_OUT, null, "Connection timed out! (could not initiate handshake)");
        }
    }


    /**
     * Start authentication and then connect to the socket if the authentication was fufilled
     *
     * @throws IOException
     */
    private void connect() throws IOException {
        //open a socket channel, create a socket reader with a default buffer, and then try to authenticate over the newly created socket.
        //the default buffer is common in everything of Fire-Io, when bigger data is getting send it will change in the whole network to what ever is needed.
        socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(true);
        if (reader != null && reader.isAlive()) reader.stop();
        //create reader
        IoReader ioReader = new IoReader(socketChannel, this, client);
        this.ioManager = new IoManager(socketChannel, InstanceSide.CLIENT, client);
        this.ioManager.setPacketHandler(input -> onPacket(input));
        socketChannel.configureBlocking(true);
        reader = new Thread(ioReader);
        //start reader
        reader.start();
        //start handshake process
        emit(new AuthPacket(identifier.toString(), System.getProperty("os.name"), arguments, argumentsMeta));
    }


    /**
     * Send a raw packet
     *
     * @param p
     * @throws IOException
     */
    public void emit(Packet p) throws IOException {
        this.ioManager.send(p);
    }


    /**
     * Force close the connection
     */
    public void close() {
        try {
            this.timer.cancel();
            //let the server know we intend to close the connection, this prevents falsely labeled errors.
            if (isSetup) emit(new PrepareClosingConnection());
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


    /**
     * A packet was received, now handle the packet and check for internal ussage
     *
     * @param packet
     */
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
            client.getEventHandler().triggerEvent(Event.CONNECT, null, "finished handshake.");
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
            client.getEventHandler().triggerPacket(null, packetPacket.getPacket(), packetPacket.getChannel());
            return;
        }

        //server has triggered a custom event with a string as payload
        if (packet instanceof ChannelMessagePacket) {
            //cast the correct internal packet
            ChannelMessagePacket message = (ChannelMessagePacket) packet;
            //trigger event handler with channel and newly created text based payload
            client.getEventHandler().triggerTextChannel(null, message.getChannel(), message.getText());
            return;
        }
    }


    /**
     * Required by interface, but not used on the client side
     *
     * @param webSocketTransaction
     */
    @Override
    public void onWebSocketPacket(WebSocketTransaction webSocketTransaction) {
        //not handled in this instance.
    }


    /**
     * this function is called when the connection has died or if closed
     * this also triggeres the appropriate events
     */
    @Override
    public void onClose() {
        //the ioreader detected that the channel closed or dropped connection
        //stop the reader, its no longer of any use since it died
        reader.stop();

        //check if it was expected to close
        if (exptectedClosing) {
            //it was expected to close, either we or the server has requested it
            client.getEventHandler().triggerEvent(Event.DISCONNECT, null, "Connection closed by request.");
        } else {
            //MURDER!
            //it timed out or died a un expected death!
            //trigger event and possibly trigger auto reconnect (if set)
            client.getEventHandler().triggerEvent(Event.TIMED_OUT, null, "Client connection unexpectedly");
        }
    }


    /**
     * This function is called when a connection opens,
     * in this case, it resets the expectedClosing function
     */
    @Override
    public void onOpen() {
        //a EMPTY & DEFAULT connection opened, reset default values
        exptectedClosing = false;
        //(dent send anything yet since we are not authenticated yet and thus the server will ignore it)
    }


    /**
     * get the active IoManager
     *
     * @return active IoManager
     */
    @Override
    public IoManager getIoManager() {
        return ioManager;
    }


    /**
     * send a message over a channel
     *
     * @param channel
     * @param message
     */
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


    /**
     * send a packet over a channel
     *
     * @param channel
     * @param packet
     */
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


    /**
     * get the client Id
     *
     * @return UUID assigned by the server
     */
    @Override
    public UUID getId() {
        //function from the ConnectedFireioClient interface for use in the API
        //return client uuid
        return identifier;
    }
}
