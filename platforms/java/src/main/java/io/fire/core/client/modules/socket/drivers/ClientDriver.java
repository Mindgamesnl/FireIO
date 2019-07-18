package io.fire.core.client.modules.socket.drivers;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.events.enums.Event;
import io.fire.core.common.io.socket.OpHandle;
import io.fire.core.common.io.socket.interfaces.NetworkDriver;
import io.fire.core.common.io.socket.interfaces.Packager;
import io.fire.core.common.io.socket.interfaces.Packet;

import io.fire.core.common.io.socket.packets.ClusterPacket;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientDriver implements NetworkDriver {

    private FireIoClient fireIoClient;
    private Socket socket;
    private Queue<Packager> packetQueue = new ConcurrentLinkedQueue<>();
    private int packetsWithoutConfirmation = 0;
    private Boolean isHandlingAPacket = false;
    private List<Packager> clusterQueue = new ArrayList<>();

    public ClientDriver(FireIoClient fireIoClient, Socket socket) {
        this.fireIoClient = fireIoClient;
        this.socket = socket;
    }

    /**
     * on error method
     * gets called when a internal error occurs with the networking
     */
    @Override
    public void onError() {

    }

    /**
     * on channel open method
     * gets called when the socket is imitated and open
     */
    @Override
    public void onOpen() {
        // send authentication packet
        try {
            send(new Packager(fireIoClient.getSocketModule().getClientDetails()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * on channel close
     * gets called when the pipeline gets closed, no matter from what end
     */
    @Override
    public void onClose() {
        fireIoClient.getEventHandler().triggerEvent(Event.DISCONNECT, "Connection closed");
    }

    /**
     * on data method
     * gets called when binary data gets received over the pipe
     * @param data payload in bytes
     */
    @Override
    public void onData(byte[] data) {
        handle(new Packager(new String(data)));
    }

    /**
     * handle decoded packet from a to z
     * @param packager decoded packet
     */
    private void handle(Packager packager) {
        try {
            // decide what packet type and what to do with it
            if (packager.isInternal()) {

                // check if packet marks op_allow_write
                if (packager.getOpHandle() == OpHandle.HANDLED_PACKET) {
                    ClusterPacket clusterPacket = (ClusterPacket) packager.getPayloadAsObject();
                    for (Packager missed : clusterPacket.getCluster()) {
                        handle(missed);
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
                    Boolean isReady = true;
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

    /**
     * Propose a packet to be send.
     * May not be send instantly since it can be queued
     * @param packager the content that is requested to send
     * @throws IOException pipe failure
     */
    private void send(Packager packager) throws IOException {
        if (isHandlingAPacket) {
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

    /**
     * Propose a packet to be send to a channel event.
     * May not be triggered instantly since it can be queued
     * @param channel channel name
     * @param packet packet payload
     * @throws IOException pipe error
     */
    public void send(String channel, Packet packet) throws IOException {
        send(new Packager(channel, packet));
    }

    /**
     * Propose a message to be send to a channel event.
     * May not be triggered instantly since it can be queued
     * @param channel channel name
     * @param message the text message
     * @throws IOException pipe error
     */
    public void send(String channel, String message) throws IOException {
        send(new Packager(channel, message));
    }
}
