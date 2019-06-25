package io.fire.core.client.modules.socket.drivers;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.io.socket.interfaces.NetworkDriver;
import io.fire.core.common.io.socket.interfaces.Packager;
import io.fire.core.common.io.socket.interfaces.Packet;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class ClientDriver implements NetworkDriver {

    private FireIoClient fireIoClient;
    private Socket socket;
    private Queue<Packager> packetQueue = new LinkedList<>();
    private int packetsWithoutConfirmation = 0;

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
            for (int i=0 ; i<1000 ; i++) {
                send(new Packager(fireIoClient.getSocketModule().getClientDetails()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onData(byte[] data, Integer length) {
        try {
            Packager packager = new Packager(new String(data));

            System.out.println("got packet!");

            // decide what packet type and what to do with it
            if (packager.isInternal()) {

                // check if packet marks op_allow_write
                if (packager.getHeader("-f-op").equals("op_allow_write")) {
                    packetsWithoutConfirmation--;
                    if (this.packetQueue.size() != 0) this.socket.getChannel().write(packetQueue.poll().getBuffer());
                    return;
                }

                //TODO: handle internal packet
                Packet packet = (Packet) packager.getPayloadAsObject();

            }
            else if (packager.hasStringBody()) {
                // handle string event
                fireIoClient.getEventHandler().triggerTextChannel(packager.getChannel(), packager.getBodyAsString());
            }
            else if (packager.hasPacketBody()) {
                // handle packet with event
                fireIoClient.getEventHandler().triggerPacket((Packet) packager.getPayloadAsObject(), packager.getChannel());
            }

            this.socket.getChannel().write(new Packager().setHeader("-f-op", "op_allow_write").getBuffer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Packager packager) throws IOException {
        if (packetQueue.size() == 0 && packetsWithoutConfirmation == 0) {
            packetsWithoutConfirmation++;
            this.socket.getChannel().write(packager.getBuffer());
        } else {
            System.out.println("Adding to queue");
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
