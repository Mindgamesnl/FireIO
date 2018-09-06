package io.fire.core.server.modules.client.objects;

import io.fire.core.common.interfaces.ClientMeta;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.io.enums.ConnectionType;
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

    //this class is the main client object for the server side
    //it contains the api functions and holds everything together

    //connection handler
    @Getter private SocketClientHandler handler;
    //client meta
    @Setter private ClientInfo info;
    private FireIoServer server;

    //create client and set random id
    public FireIoConnection(FireIoServer server) {
        setId(UUID.randomUUID());
        this.server = server;
    }

    @Override
    public void send(String channel, Packet packet) {
        //function from the Client interface (api)
        //send a custom packet over a channel
        //check if there is a handler, if the handshake is finished and if it's accepting data
        if (handler != null && handler.authenticated && handler.isOpen()) {
            try {
                //create internal channel packet with the channel and custom packet, then send it over the handler
                handler.emit(new ChannelPacketPacket(null, channel, packet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ClientMeta getMeta(String key) {
        //function from the Client interface (api)
        //get meta by key
        return info.getArgumentsMeta().get(key);
    }

    @Override
    public String getTag(String key) {
        //function from the Client interface (api)
        //get argument by key
        return info.getArguments().get(key);
    }

    @Override
    public ClientInfo getInfo() {
        //function from the Client interface (api)
        //get client information
        return info;
    }

    @Override
    public void send(String channel, String message) {
        //function from the Client interface (api)
        //send a string over a channel
        //check if there is a handler, if the handshake is finished and if it's accepting data
        if (handler != null && handler.authenticated && handler.isOpen()) {
            try {
                //create a internal packet with the channel name and string, then send it via the handler
                handler.emit(new ChannelMessagePacket(channel, message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ConnectionType getConnectionType() {
        return this.handler.getConnectionType();
    }

    @Override
    public void close() {
        //function from the Client interface (api)
        //close the connection (kick the client from the network)
        //check if there is a handler
        if (handler != null) {
            //check if the connection is actually open
            if (handler.isOpen()) {
                //handle socket closure in the socket handler that belongs to this connection
                handler.close();
            }
        }
        //trigger disconnect event with this class as payload
        server.getEventHandler().fireEvent(Event.DISCONNECT, this);
        //remove client from the client manager (deletes this object, the handler and id)
        //this prevent memory leaks
        server.getClientModule().removeClient(getId());
    }

    public void setHandler(SocketClientHandler handler) {
        //set the handler thats given to this connection
        //this happens every time a handshake finishes successfully
        this.handler = handler;
        //register packet handler from the socket handler (yo)
        handler.onMessage(packet -> {
            //check if it is a channel message packet
            if (packet instanceof ChannelMessagePacket) {
                //cast correct packet object
                ChannelMessagePacket messagePacket = (ChannelMessagePacket) packet;
                //create payload with the channel, string and this client subclass as sender
                //the Client object can be cast to FireIoConnection to get the handler
                server.getEventHandler().fireEvent(
                        messagePacket.getChannel(),
                        new ReceivedText(messagePacket.getText(),
                                this));
                //stop running since we are finished
                return;
            }

            //check if is a custom packet
            if (packet instanceof ChannelPacketPacket) {
                //cast correct internal packet
                ChannelPacketPacket packetPacket = (ChannelPacketPacket) packet;
                //set this client as the sender
                packetPacket.setSender(this);
                //trigger the correct event channel with the payload
                server.getEventHandler().fireEvent(packetPacket.getChannel(), packetPacket);
            }
        });
    }
}
