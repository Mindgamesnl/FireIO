package io.fire.core.client.modules.socket.drivers;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.events.enums.Event;
import io.fire.core.common.io.socket.OpHandle;
import io.fire.core.common.io.socket.interfaces.NetworkDriver;
import io.fire.core.common.io.socket.interfaces.Packager;
import io.fire.core.common.io.socket.interfaces.Packet;

import io.fire.core.common.io.socket.packets.ClusterPacket;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientDriver implements NetworkDriver {

    private FireIoClient fireIoClient;
    private Socket socket;
    private Queue<Packager> packetQueue = new ConcurrentLinkedQueue<>();
    private int packetsWithoutConfirmation = 0;
    private Boolean isReady = false;
    private Boolean isHandlingAPacket = false;
    private List<Packager> clusterQueue = new ArrayList<>();

    public ClientDriver(FireIoClient fireIoClient, Socket socket) {
        this.fireIoClient = fireIoClient;
        this.socket = socket;
    }

    @Override
    public void onError() {

    }

    @Override
    public void onOpen() {
        // send authentication packet
        try {
            send(new Packager(fireIoClient.getSocketModule().getClientDetails()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose() {
        fireIoClient.getEventHandler().triggerEvent(Event.DISCONNECT, "Connection closed");
    }

    @Override
    public void onData(byte[] data, Integer length) {
        handle(new Packager(new String(data)), length);
    }

    private void handle(Packager packager, Integer length) {
        try {
            // decide what packet type and what to do with it
            if (packager.isInternal()) {

                // check if packet marks op_allow_write
                if (packager.getOpHandle() == OpHandle.HANDLED_PACKET) {
                    ClusterPacket clusterPacket = (ClusterPacket) packager.getPayloadAsObject();
                    System.out.println(packager);
                    for (Packager missed : clusterPacket.getCluster()) {
                        handle(missed, -1);
                    }

                    if (packetsWithoutConfirmation != 0) packetsWithoutConfirmation--;
                    if (this.packetQueue.size() != 0) {
                        this.socket.getChannel().write(packetQueue.poll().getBuffer());
                    }
                    return;
                }

                // check if all is ready
                if (packager.getOpHandle() == OpHandle.READY) {
                    packetsWithoutConfirmation = 0;
                    this.packetQueue.clear();
                    fireIoClient.getEventHandler().triggerEvent(Event.CONNECT, "Open and ready");
                    this.socket.getChannel().write(new Packager(OpHandle.HANDLED_PACKET).setHeader("f-has-packet", "yes").setBody(new ClusterPacket(clusterQueue)).getBuffer());
                    isReady = true;
                    return;
                }

                // check if auth was invalid
                if (packager.getOpHandle() == OpHandle.INVALID_AUTH) {
                    fireIoClient.getEventHandler().triggerEvent(Event.DISCONNECT, "Invalid auth!");
                    this.socket.close();
                    return;
                }

                //TODO: handle internal packet
                return;

            }

            isHandlingAPacket = true;

            if (packager.hasStringBody()) {
                // handle string event
                fireIoClient.getEventHandler().triggerTextChannel(packager.getChannel(), packager.getBodyAsString());
            }
            else if (packager.hasPacketBody()) {
                // handle packet with event
                fireIoClient.getEventHandler().triggerPacket((Packet) packager.getPayloadAsObject(), packager.getChannel());
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
            System.out.println("Sending packet");
        } else {
            this.packetQueue.add(packager);
            System.out.println("Queuing packet");
        }
    }

    public void send(String channel, Packet packet) throws IOException {
        send(new Packager(channel, packet));
    }

    public void send(String channel, String packet) throws IOException {
        send(new Packager(channel, packet));
    }
}
