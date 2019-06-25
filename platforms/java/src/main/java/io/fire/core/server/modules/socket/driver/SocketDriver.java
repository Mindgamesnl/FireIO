package io.fire.core.server.modules.socket.driver;

import io.fire.core.client.modules.socket.objects.ClientDetails;
import io.fire.core.common.events.enums.Event;
import io.fire.core.common.io.socket.OpHandle;
import io.fire.core.common.io.socket.interfaces.GenericClient;
import io.fire.core.common.io.socket.interfaces.Packager;
import io.fire.core.common.io.socket.interfaces.Packet;
import io.fire.core.common.io.socket.packets.ClusterPacket;
import io.fire.core.server.FireIoServer;
import io.fire.core.common.io.socket.interfaces.NetworkDriver;
import io.fire.core.server.modules.socket.objects.Client;
import io.fire.core.server.modules.socket.objects.Connection;
import lombok.Getter;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketDriver implements NetworkDriver {

    private Socket socket;
    private FireIoServer main;
    private Connection connection;
    private GenericClient genericClient;
    @Getter private ClientDetails clientDetails;
    private Queue<Packager> packetQueue = new ConcurrentLinkedQueue<>();
    private int packetsWithoutConfirmation = 0;
    private Boolean isReady = false;
    private Boolean isHandlingAPacket = false;
    private List<Packager> clusterQueue = new ArrayList<>();

    SocketDriver(Socket socket, FireIoServer main, Connection connection) {
        this.socket = socket;
        this.main = main;
        this.connection = connection;
    }

    @Override
    public void onError() {
        System.out.println("Error");
    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onClose() {
        main.getEventHandler().triggerEvent(Event.DISCONNECT, genericClient, "Client disconnected");
    }

    @Override
    public void onData(byte[] data, Integer length) {
        handle(new Packager(new String(data)), length);
    }

    private void handle(Packager packager, Integer length) {
        try {
            if (packager.isInternal()) {

                // check if packet marks op_allow_write
                if (packager.getOpHandle() == OpHandle.HANDLED_PACKET) {

                    // howmany did we miss?
                    ClusterPacket clusterPacket = (ClusterPacket) packager.getPayloadAsObject();
                    for (Packager missed : clusterPacket.getCluster()) {
                        System.out.println("it has a missed packet, lets handle it");
                        handle(missed, -1);
                    }

                    if (packetsWithoutConfirmation != 0) packetsWithoutConfirmation--;
                    if (this.packetQueue.size() != 0) {
                        this.socket.getChannel().write(packetQueue.poll().getBuffer());
                    }
                    return;
                }

                //TODO: handle internal packet
                Packet packet = (Packet) packager.getPayloadAsObject();

                if (packet instanceof ClientDetails) {
                    // apply client details
                    clientDetails = (ClientDetails) packet;

                    // check if password is correct, and optional other stuff
                    if (!main.getSocketServer().validatePassword(clientDetails.getPassword())) {
                        this.socket.getChannel().write(new Packager(OpHandle.INVALID_AUTH).getBuffer());
                        return;
                    } else {
                        isReady = true;
                        this.clientDetails.setUuid(UUID.randomUUID());
                        this.genericClient = new Client(this);
                        //TODO : this is probably the problem
                        this.socket.getChannel().write(new Packager(OpHandle.READY).getBuffer());
                        main.getEventHandler().triggerEvent(Event.CONNECT, genericClient, "Authenticated and connected");
                        return;
                    }
                }
            }
            else if (!isReady) {
                // not ready
                return;
            }

            isHandlingAPacket = true;

            // message that we can get the new packet if the other side had a queue
            if (packager.hasStringBody()) {
                // handle string with event
                main.getEventHandler().triggerTextChannel(genericClient, packager.getChannel(), packager.getBodyAsString());
            }
            else if (packager.hasPacketBody()) {
                // handle packet with event
                main.getEventHandler().triggerPacket(genericClient, packager.getChannel(), (Packet) packager.getPayloadAsObject());
            }

            isHandlingAPacket = false;

            this.socket.getChannel().write(new Packager(OpHandle.HANDLED_PACKET).setHeader("f-has-packet", "yes").setBody(new ClusterPacket(clusterQueue)).getBuffer());
            clusterQueue.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Packager packager) throws IOException {
        if (isHandlingAPacket) {
            System.out.println("Canceled since i am handling a packet!");
            clusterQueue.add(packager);
            return;
        }

        if (packetQueue.size() == 0 && packetsWithoutConfirmation == 0) {
            packetsWithoutConfirmation++;
            this.socket.getChannel().write(packager.getBuffer());
        } else {
            this.packetQueue.add(packager);
        }
    }

    public void send(String channel, Packet packet) throws IOException {
        send(new Packager(channel, packet));
    }

    public void send(String channel, String packet) throws IOException {
        send(new Packager(channel, packet));
    }
}
